package com.cambiahealth.ahs;

import com.cambiahealth.ahs.entity.AcorsEligibility;
import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.FlatFileResolverFactory;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.processors.AddressProcessor;
import com.cambiahealth.ahs.processors.CobProcessor;
import com.cambiahealth.ahs.processors.EligibilityProcessor;
import com.cambiahealth.ahs.processors.NameProcessor;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePeriod;
import org.joda.time.Years;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException, ParseException, InvalidArgumentException {
        String basePath;
        if(args.length < 1) {
            throw new InvalidArgumentException(new String[]{"Must pass base path as first argument"});
        }

        basePath = args[0];

        Map<FileDescriptor, String> descriptors = new HashMap<FileDescriptor, String>();
        descriptors.put(FileDescriptor.ACORS_ELIGIBILITY_EXTRACT, basePath + "OOA_Acors_Extract.dat");
        descriptors.put(FileDescriptor.CLAIMS_CONFIG_EXTRACT, basePath + "OOA_Claims_Extract.dat");
        descriptors.put(FileDescriptor.COB_EXTRACT, basePath + "OOA_COB_Extract.dat");
        descriptors.put(FileDescriptor.CONFIDENTIAL_ADDRESS_EXTRACT, basePath + "OOA_Conf_Address_Extract.dat");
        descriptors.put(FileDescriptor.CONFIDENTIAL_EMAIL_PHONE_EXTRACT, basePath + "OOA_ConfEmailPhone_Extract.dat");
        descriptors.put(FileDescriptor.CSPI_EXTRACT, basePath + "OOA_CSPI_Extract.dat");
        descriptors.put(FileDescriptor.MEMBER_HISTORY_EXTRACT, basePath + "OOA_Member_Extract.dat");
        descriptors.put(FileDescriptor.SUBSCRIBER_ADDRESS_EXTRACT, basePath + "OOA_Sub_Address_Extract.dat");
        descriptors.put(FileDescriptor.ZIP_CODE_EXTRACT, basePath + "OOA_Zipcode_Extract.dat");
        descriptors.put(FileDescriptor.BCBSA_MBR_PFX_SFX_XREF, basePath + "OOA_Title_Extract.dat");
        descriptors.put(FileDescriptor.FINAL_2A_OUTPUT, basePath + "ndw_member");

        FlatFileResolverFactory factory = new FlatFileResolverFactory(false);
        IFlatFileResolver resolver = factory.getInstance(descriptors);

        create2A(resolver);

        System.out.println("I'm a java JAR!");
    }

    public static void create2A(IFlatFileResolver resolver) throws IOException, ParseException {
        initializeProcessors(resolver);

        beginProcessing(resolver);

        shutdownProcessors();
    }

    private static void initializeProcessors(IFlatFileResolver resolver) throws IOException {
        // COB init()
        CobProcessor.initialize(resolver);

        // Address init()
        AddressProcessor.initialize(resolver);

        // NameProcessor init()
        NameProcessor.initialize(resolver);

        // Eligibility init()
        EligibilityProcessor.initialize(resolver);
    }

    private static void shutdownProcessors() throws IOException {
        // COB shutdown()
        CobProcessor.shutdown();

        // Address shutdown()
        AddressProcessor.shutdown();

        // NameProcessor shutdown()
        NameProcessor.shutdown();

        // Eligibility shutdown()
        EligibilityProcessor.shutdown();
    }

    private static void beginProcessing(IFlatFileResolver resolver) throws IOException, ParseException {
        FlatFileReader reader = resolver.getFile(FileDescriptor.ACORS_ELIGIBILITY_EXTRACT);
        BufferedWriter writer = resolver.writeFile(FileDescriptor.FINAL_2A_OUTPUT);

        String memeCk = null;
        Map<String, String> currentLine = null;

        // Walk through AcorsEligibility
        while(null != (currentLine = reader.readColumn())) {
            Map<TimelineContext, Timeline> timelines = new HashMap<TimelineContext, Timeline>();
            String lineMemeCk = currentLine.get(AcorsEligibility.MEME_CK.toString());

            // If MEME has changed, then process new row
            // If meme has not changed move on
            if(StringUtils.equals(lineMemeCk, memeCk)) {
               continue;
            }


            // Start the next row
            memeCk = lineMemeCk;

            if(processMeme(lineMemeCk, timelines)) {
                outputRowTo2A(writer, timelines);
            }
        }
    }

    private static boolean processMeme(String meme, Map<TimelineContext, Timeline>  timelines) throws IOException, ParseException {
        // Address process()
        Timeline address = AddressProcessor.processAddress(meme, timelines);
        if(address.isEmpty()) {
            return false;
        }

        // Eligibility process()
        Timeline elig = EligibilityProcessor.processEligibiltiy(meme, timelines);
        if(elig.isEmpty()) {
            return false;
        }

        // COB process()
        CobProcessor.processCob(meme, timelines);

        // NameProcessor process()
        NameProcessor.processName(meme, timelines);

        return true;
    }

    static void outputRowTo2A(BufferedWriter writer, Map<TimelineContext, Timeline> timelines) {
        Timeline cob = timelines.get(TimelineContext.COB);
        Timeline name = timelines.get(TimelineContext.NAME);
        Timeline primaryAddress = timelines.get(TimelineContext.ADDRESS_PRIMARY);
        Timeline secondaryAddress = timelines.get(TimelineContext.ADDRESS_SECONDARY);
        Timeline eligibility = timelines.get(TimelineContext.ELIGIBILITY);

        Timeline cane = new Timeline();
        HashMap<String, String> curDay = new HashMap<String, String>();
        HashMap<String, String> nextDay = new HashMap<String, String>();
        Map<String,String> testMap;
        LocalDate today = new LocalDate();
        today = today.minusDays(today.getDayOfMonth());
        LocalDate twoYear = today.minusYears(2);
        LocalDate threeYear = today.minusYears(3);
        LocalDate date = today;
        LocalDate tmpEnd = date;

        while(date.compareTo(threeYear)>0){


            curDay.putAll(cob.get(date));
            testMap = name.get(date);
            if(!testMap.isEmpty()) {
                curDay.putAll(testMap);
            }else{
                date = date.minusDays(1);
                continue;
            }
            testMap = primaryAddress.get(date);
            if(!testMap.isEmpty()) {
                curDay.putAll(testMap);
            }else{
                date = date.minusDays(1);
                continue;
            }
            curDay.putAll(secondaryAddress.get(date));
            curDay.putAll(eligibility.get(date));

            if(date.equals(today)){
                nextDay = curDay;
                date = date.minusDays(1);
                continue;
            }
            if(!nextDay.equals(curDay)){
                if(tmpEnd.compareTo(twoYear)>0){
                    cane.storeVector(date.plusDays(1),tmpEnd,nextDay);
                }
                tmpEnd = date;
            }

            nextDay = curDay;

        }
    }
}
