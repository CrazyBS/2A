package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.ConfidentialAddress;
import com.cambiahealth.ahs.entity.MemberHistory;
import com.cambiahealth.ahs.entity.SubscriberAddress;
import com.cambiahealth.ahs.entity.ZipCode;
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
    private static FlatFileReader zipReader;
    private static Set<String> zipCodes;

    private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public static void initialize(IFlatFileResolver resolver) throws IOException {
        confReader = resolver.getFile(FileDescriptor.CONFIDENTIAL_ADDRESS_EXTRACT);
        subReader = resolver.getFile(FileDescriptor.SUBSCRIBER_ADDRESS_EXTRACT);
        zipReader = resolver.getFile(FileDescriptor.ZIP_CODE_EXTRACT);
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
        zipReader = null;
    }

    public static Timeline processAddress(String MEME, Map<TimelineContext, Timeline> timelines) throws IOException, ParseException {
        Timeline confTimeline = new Timeline();
        Timeline homeTimeline = new Timeline();
        Timeline mailTimeline = new Timeline();
        Timeline rejectTimeline = new Timeline();
        Timeline rejectMailTimeline = new Timeline();
        Timeline rejectHomeTimeline = new Timeline();
        Timeline primaryTimeline = new Timeline();
        Timeline secondaryTimeline = new Timeline();

        while(true) {
            Map<String, String> line;
            line = confReader.readColumn();

            if (line != null) {
                int rowTest = line.get(ConfidentialAddress.MEME_CK.toString()).compareTo(MEME);
                if (rowTest < 0) {
                    continue;
                } else if(rowTest == 0) {
                    if(!line.get(ConfidentialAddress.ENAD_STATE.toString()).equals("WA") || (line.get(ConfidentialAddress.ENAD_STATE.toString()).equals("WA") && zipCodes.contains(line.get(ConfidentialAddress.ENAD_ZIP.toString())))){
                        confTimeline.storeVector(new LocalDate(format.parse(line.get(ConfidentialAddress.PMCC_EFF_DT.toString()))), new LocalDate(format.parse(line.get(ConfidentialAddress.PMCC_TERM_DTM.toString()))), line);
                    } else {
                        rejectTimeline.storeVector(new LocalDate(format.parse(line.get(ConfidentialAddress.PMCC_EFF_DT.toString()))), new LocalDate(format.parse(line.get(ConfidentialAddress.PMCC_TERM_DTM.toString()))), line);
                    }
                } else {
                    confReader.unRead();
                    break;
                }
            } else {
                break;
            }
        }

        while(true) {
            Map<String, String> line;
            line = subReader.readColumn();

            if (line != null) {
                int rowTest = line.get(SubscriberAddress.MEME_CK.toString()).compareTo(MEME);
                if (rowTest < 0) {
                    continue;
                } else if(rowTest == 0) {
                    if(line.get(SubscriberAddress.SBAD_TYPE.toString()).equals("H")){
                        homeTimeline.storeVector(new LocalDate(format.parse(line.get(SubscriberAddress.SBSB_EFF_DT.toString()))), new LocalDate(format.parse(line.get(SubscriberAddress.SBSB_TERM_DT.toString()))), line);
                        if(!line.get(SubscriberAddress.SBAD_STATE.toString()).equals("WA") || (line.get(SubscriberAddress.SBAD_STATE.toString()).equals("WA") && zipCodes.contains(line.get(SubscriberAddress.SBAD_ZIP.toString())))){
                            rejectHomeTimeline.storeVector(new LocalDate(format.parse(line.get(SubscriberAddress.SBSB_EFF_DT.toString()))), new LocalDate(format.parse(line.get(SubscriberAddress.SBSB_TERM_DT.toString()))), line);
                        }
                    } else if(line.get(SubscriberAddress.SBAD_TYPE.toString()).equals("M")){
                        mailTimeline.storeVector(new LocalDate(format.parse(line.get(SubscriberAddress.SBSB_EFF_DT.toString()))), new LocalDate(format.parse(line.get(SubscriberAddress.SBSB_TERM_DT.toString()))), line);
                        if(!line.get(SubscriberAddress.SBAD_STATE.toString()).equals("WA") || (line.get(SubscriberAddress.SBAD_STATE.toString()).equals("WA") && zipCodes.contains(line.get(SubscriberAddress.SBAD_ZIP.toString())))){
                            rejectMailTimeline.storeVector(new LocalDate(format.parse(line.get(SubscriberAddress.SBSB_EFF_DT.toString()))), new LocalDate(format.parse(line.get(SubscriberAddress.SBSB_TERM_DT.toString()))), line);
                        }
                    }
                } else {
                    subReader.unRead();
                    break;
                }
            } else {
                break;
            }
        }

        homeTimeline.removeAll(rejectTimeline);
        mailTimeline.removeAll(rejectTimeline);

        Timeline primaryHome = new Timeline();
        primaryHome.addAll(homeTimeline);
        primaryHome.removeAll(rejectHomeTimeline);

        Timeline primaryMail = new Timeline();
        primaryMail.addAll(mailTimeline);
        primaryMail.removeAll(rejectMailTimeline);

        primaryTimeline.addAll(primaryMail);
        primaryTimeline.addAll(primaryHome);
        primaryTimeline.addAll(confTimeline);

        secondaryTimeline.addAll(mailTimeline);

        Timeline primaryNotMail = new Timeline();
        primaryNotMail.addAll(primaryHome);
        primaryNotMail.addAll(confTimeline);

        mailTimeline.removeAll(primaryNotMail);

        secondaryTimeline.removeAll(mailTimeline);

        timelines.put(TimelineContext.ADDRESS_PRIMARY, primaryTimeline);
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
}
