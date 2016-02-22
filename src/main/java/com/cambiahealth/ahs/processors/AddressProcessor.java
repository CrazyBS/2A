package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.*;
import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by msnook on 2/18/2016.
 */
public class AddressProcessor {
    private static FlatFileReader confReader;
    private static FlatFileReader subReader;
    private static Set<String> zipCodes;
    private static String[] inAreaStates = { "OR", "UT", "ID" };
    private static String[] outOfAreaStates = {"AL","AK","AZ","AR","CA","CO","CT","DE","DC","FL","GA","HI","IL","IN","IA","KS","KY","LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV","NH","NJ","NM","NY","NC","ND","OH","OK","PA","RI","SC","SD","TN","TX","VT","VA","WV","WI","WY"};

    private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public static void initialize(IFlatFileResolver resolver) throws IOException {
        confReader = resolver.getFile(FileDescriptor.CONFIDENTIAL_ADDRESS_EXTRACT);
        subReader = resolver.getFile(FileDescriptor.SUBSCRIBER_ADDRESS_EXTRACT);
        FlatFileReader zipReader = resolver.getFile(FileDescriptor.ZIP_CODE_EXTRACT);
        zipCodes = new HashSet<String>();

        while(true){
            Map<String, String> line;
            line = zipReader.readColumn();
            if(line == null){
                break;
            } else {
                zipCodes.add(line.get(ZipCode.ZIP_CODE.toString()));
            }
        }

        zipReader.close();
    }

    public static Timeline processAddress(String MEME, Map<TimelineContext, Timeline> timelines) throws IOException, ParseException {
        Timeline confTimeline = new Timeline();
        Timeline homeTimeline = new Timeline();
        Timeline mailTimeline = new Timeline();
        Timeline mailingForSecondaryTimeline = new Timeline();
        Timeline rejectConfTimeline = new Timeline();
        Timeline rejectMailTimeline = new Timeline();
        Timeline rejectHomeTimeline = new Timeline();
        Timeline primaryTimeline = new Timeline();
        Timeline secondaryTimeline = new Timeline();

        while(true) {
            Map<String, String> line;
            line = confReader.readColumn();

            // Out of lines?
            if (null == line) {
                break;
            }

            int rowTest = line.get(ConfidentialAddress.MEME_CK.toString()).compareTo(MEME);
            if(rowTest == 0) {
                String state = line.get(ConfidentialAddress.ENAD_STATE.toString());
                String zip = line.get(ConfidentialAddress.ENAD_ZIP.toString());
                LocalDate startDate = new LocalDate(format.parse(line.get(ConfidentialAddress.PMCC_EFF_DT.toString())));
                LocalDate endDate = new LocalDate(format.parse(line.get(ConfidentialAddress.PMCC_TERM_DTM.toString())));

                if(isValidOutOfArea(state, zip)){
                    confTimeline.storeVector(startDate, endDate, line);
                    confTimeline.addConsistentData(ConsistentFields.IS_PHI.toString(), "PHI");
                } else {
                    rejectConfTimeline.storeVector(startDate, endDate, line);
                }
            } else if (rowTest > 0){
                // Did we pass it?
                confReader.unRead();
                break;
            }
        }

        while(true) {
            Map<String, String> line;
            line = subReader.readColumn();

            // Out of lines?
            if(null == line) {
                break;
            }

            int rowTest = line.get(SubscriberAddress.MEME_CK.toString()).compareTo(MEME);
            if(rowTest == 0) {
                String state = line.get(SubscriberAddress.SBAD_STATE.toString());
                String zip = line.get(SubscriberAddress.SBAD_ZIP.toString());
                LocalDate startDate = new LocalDate(format.parse(line.get(SubscriberAddress.SBSB_EFF_DT.toString())));
                LocalDate endDate = new LocalDate(format.parse(line.get(SubscriberAddress.SBSB_TERM_DT.toString())));

                if(line.get(SubscriberAddress.SBAD_TYPE.toString()).equals("H")){
                    if(isValidOutOfArea(state, zip)) {
                        homeTimeline.storeVector(startDate, endDate, line);
                    } else {
                        rejectHomeTimeline.storeVector(startDate, endDate, line);
                    }
                } else if(line.get(SubscriberAddress.SBAD_TYPE.toString()).equals("M")){
                    // Always add the mailing timeline for the secondary addresses, but only if it is a valid state.  There is lots of garbage out there
                    if (isValidState(state)) {
                        mailTimeline.storeVector(startDate, endDate, line);

                        Map<String, String> addressForSecondary = new HashMap<String, String>(line.size());
                        for(String key : line.keySet()) {
                            addressForSecondary.put("secd_" + key, line.get(key));
                        }
                        mailingForSecondaryTimeline.storeVector(startDate, endDate, addressForSecondary);
                    }
                    if (!isValidOutOfArea(state, zip)) {
                        rejectMailTimeline.storeVector(startDate, endDate, line);
                    }
                }
            } else if (rowTest > 0){
                // Did we pass it?
                subReader.unRead();
                break;
            }
        }

        // Logic statement:
        // Confidential must be in primary address
        // If confidential is NOT in area, it must be removed

        // A subscriber address H can only go into the Primary, if there is no confidential
        // A mailing address goes into the secondary, unless there is no primary, then it become primary

        // Possible plan for primary
        // Paint all mailing addresses onto the primary line
        primaryTimeline.addAll(mailTimeline);

        // Paint all rejected mailing onto the primary line
        primaryTimeline.removeAll(rejectMailTimeline);

        // Paint all the home addresses onto the primary line
        primaryTimeline.addAll(homeTimeline);

        // Paint all the rejected home addresses onto the primary line
        primaryTimeline.removeAll(rejectHomeTimeline);

        // Paint all the conf addresses onto the primary line
        primaryTimeline.addAll(confTimeline);

        // Paint all the rejected conf addresses on the primary line
        primaryTimeline.removeAll(rejectConfTimeline);

        // Store the primary as complete
        timelines.put(TimelineContext.ADDRESS_PRIMARY, primaryTimeline);

        // Possible plan for secondary
        // Just store all the mailing addresses, but with a dependency of the primary addresses
        Timeline sameAsAboveNoMailing = new Timeline();
        sameAsAboveNoMailing.addAll(homeTimeline);
        sameAsAboveNoMailing.removeAll(rejectHomeTimeline);
        sameAsAboveNoMailing.addAll(confTimeline);
        sameAsAboveNoMailing.removeAll(rejectConfTimeline);

        // Add the primary addresses as a dependency to the mailing.
        // If there is no valid primary address, then the mailing will not output
        // Since the mailing isn't in the dependency, we will only be visible when valid
        // We also don"t care of about the rejected mailing lists, since all addresses are valid here
        secondaryTimeline.addAll(mailingForSecondaryTimeline, sameAsAboveNoMailing);

        timelines.put(TimelineContext.ADDRESS_SECONDARY, secondaryTimeline);

        return primaryTimeline;
    }

    public static void shutdown() throws IOException {
        confReader.close();
        confReader = null;

        subReader.close();
        subReader = null;

        zipCodes = null;
    }

    private static boolean isValidOutOfArea(String state, String zip) {
        return null != state && null != zip && (stringIn(state, outOfAreaStates) || "WA".equals(state) && zipCodes.contains(StringUtils.substring(zip, 0, 6)));
    }

    private static boolean isValidState(String state) {
        return null != state &&  (stringIn(state, outOfAreaStates) || stringIn(state, inAreaStates) || "WA".equals(state));
    }

    private static boolean stringIn(String left, String ... rightStrings) {
        for(String right : rightStrings) {
            if(left.equals(right)) {
                return true;
            }
        }
        return false;
    }
}
