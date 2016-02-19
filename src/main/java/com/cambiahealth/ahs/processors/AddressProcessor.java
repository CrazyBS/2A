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
import java.util.*;

/**
 * Created by msnook on 2/18/2016.
 */
public class AddressProcessor {
    private static FlatFileReader confReader;
    private static FlatFileReader subReader;
    private static FlatFileReader zipReader;

    public static void initialize(IFlatFileResolver resolver) throws FileNotFoundException {
        confReader = resolver.getFile(FileDescriptor.CONFIDENTIAL_ADDRESS_EXTRACT);
        subReader = resolver.getFile(FileDescriptor.SUBSCRIBER_ADDRESS_EXTRACT);
        zipReader = resolver.getFile(FileDescriptor.ZIP_CODE_EXTRACT);
    }

    public static void processAddress(String MEME, Map<TimelineContext, Timeline> timelines) throws IOException {
        Timeline confTimeline = new Timeline();
        Timeline subTimeline = new Timeline();
        Timeline primaryTimeline = new Timeline();
        Timeline secondaryTimeline = new Timeline();
        Set<String> zipCodes = new HashSet<String>();

        while(true){
            Map<String, String> line;
            line = zipReader.readColumn();
            if(line == null){
                break;
            } else {
                zipCodes.add(line.get(ZipCode.ZIP_CODE));
            }
        }

        while(true) {
            Map<String, String> line;
            line = confReader.readColumn();

            if (line != null) {
                int rowTest = line.get(ConfidentialAddress.MEME_CK).compareTo(MEME);
                if (rowTest < 0) {
                    continue;
                } else if(rowTest == 0) {
                    if(zipCodes.contains(line.get(ConfidentialAddress.ENAD_ZIP))){
                        confTimeline.storeVector(new LocalDate(line.get(ConfidentialAddress.PMCC_EFF_DT)), new LocalDate(line.get(ConfidentialAddress.PMCC_TERM_DTM)), line);
                    } else {
                        confTimeline.storeVector(new LocalDate(line.get(ConfidentialAddress.PMCC_EFF_DT)), new LocalDate(line.get(ConfidentialAddress.PMCC_TERM_DTM)), null);
                    }
                } else {
                    confReader.unRead();
                    break;
                }
            } else {
                break;
            }
            //Timeline.RemoveAll(Timeline)
            //Timeline.PutAll(Timeline)
            //Basic merge methods.  Likely unused outside of this scenario, but very useful.
            //RemoveAll will

            timelines.put(TimelineContext.ADDRESS_PRIMARY,primaryTimeline);
            timelines.put(TimelineContext.ADDRESS_SECONDARY,secondaryTimeline);
        }

        while(true) {
            Map<String, String> line;
            line = subReader.readColumn();

            if (line != null) {
                int rowTest = line.get(SubscriberAddress.MEME_CK).compareTo(MEME);
                if (rowTest < 0) {
                    continue;
                } else if(rowTest == 0) {
                    subTimeline.storeVector(new LocalDate(line.get(SubscriberAddress.SBSB_EFF_DT)), new LocalDate(line.get(SubscriberAddress.SBSB_TERM_DT)), line);
                } else {
                    subReader.unRead();
                    break;
                }
            } else {
                break;
            }
        }
    }

    public static void shutdown() throws IOException {
        confReader.close();
        confReader = null;

        subReader.close();
        subReader = null;
    }
}
