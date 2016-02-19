package com.cambiahealth.ahs;

import com.cambiahealth.ahs.entity.AcorsEligibility;
import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.FlatFileResolverFactory;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.processors.CobProcessor;
import com.cambiahealth.ahs.processors.NameProcessor;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePeriod;
import org.joda.time.Years;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class Main {

    private static ReadablePeriod lookback = Years.THREE;

    public static void main(String[] args) throws IOException, ParseException {
        Map<FileDescriptor, String> descriptors = new HashMap<FileDescriptor, String>();
        descriptors.put(FileDescriptor.ACORS_ELIGIBILITY_EXTRACT, "");
        descriptors.put(FileDescriptor.CLAIMS_CONFIG_EXTRACT, "");
        descriptors.put(FileDescriptor.COB_EXTRACT, "");
        descriptors.put(FileDescriptor.CONFIDENTIAL_ADDRESS_EXTRACT, "");
        descriptors.put(FileDescriptor.CONFIDENTIAL_EMAIL_PHONE_EXTRACT, "");
        descriptors.put(FileDescriptor.CSPI_EXTRACT, "");
        descriptors.put(FileDescriptor.MEMBER_HISTORY_EXTRACT, "");
        descriptors.put(FileDescriptor.SUBSCRIBER_ADDRESS_EXTRACT, "");
        descriptors.put(FileDescriptor.ZIP_CODE_EXTRACT, "");
        descriptors.put(FileDescriptor.FINAL_2A_OUTPUT, "");

        FlatFileResolverFactory factory = new FlatFileResolverFactory(false);
        IFlatFileResolver resolver = factory.getInstance(descriptors);

        create2A(resolver);

        // Get next Ctg
        // Loop: Get next meme

        // Process Address (don't forget zip codes)
        // If no remote addresses present, stop processing

        // Merge Eligibility with CSPI History then ClaimConfiguration
        // If no data present, stop processing
        // Store 4 "most recent" columns during this walk.

        // Process COB

        // Reduce Member History

        // Final Merging
        // Store to processed rows buffer

        // End loop condition (no more meme's in CTG set)

        // If more than one processed row pending, then
        // Post process COB

        // Flush processed rows to flat file.

        // Loop Ctg
        System.out.println("I'm a java JAR!");
    }

    private static void create2A(IFlatFileResolver resolver) throws IOException, ParseException {
        initializeProcessors(resolver);

        beginProcessing(resolver);

        shutdownProcessors();
    }

    private static void initializeProcessors(IFlatFileResolver resolver) throws FileNotFoundException {
        // COB init()
        CobProcessor.initialize(resolver);

        // Address init()

        // NameProcessor init()
        NameProcessor.initialize(resolver);

        // Eligibility init()
    }

    private static void shutdownProcessors() throws IOException {
        // COB shutdown()
        CobProcessor.shutdown();

        // Address shutdown()

        // NameProcessor shutdown()
        NameProcessor.shutdown();

        // Eligibility shutdown()
    }

    private static void beginProcessing(IFlatFileResolver resolver) throws IOException, ParseException {
        FlatFileReader reader = resolver.getFile(FileDescriptor.ACORS_ELIGIBILITY_EXTRACT);
        BufferedWriter writer = resolver.writeFile(FileDescriptor.FINAL_2A_OUTPUT);

        String ctgId = null;
        String memeCk = null;
        Map<String, String> currentLine = null;

        Deque<Map<TimelineContext, Timeline>> rawRows = new ArrayDeque<Map<TimelineContext, Timeline>>();
        Map<TimelineContext, Timeline> timelines = new HashMap<TimelineContext, Timeline>();

        // Walk through AcorsEligibility
        while(null != (currentLine = reader.readColumn())) {
            String lineCtgId = currentLine.get(AcorsEligibility.CTG_ID.toString());
            String lineMemeCk = currentLine.get(AcorsEligibility.MEME_CK.toString());

            // If MEME has changed, then process new row
            // If meme has not changed move on
            if(StringUtils.equals(lineMemeCk, memeCk)) {
               continue;
            }

            // If CTG has changed, process COB
            // then output row
            if(!StringUtils.equals(lineCtgId, ctgId)) {
                ouputAllRows(writer, rawRows);
                rawRows.clear();
            }

            // Start the next row
            ctgId = lineCtgId;
            memeCk = lineMemeCk;

            processMeme(lineMemeCk, timelines);
            rawRows.addLast(timelines);
        }

        // Output the rest of the rows
        ouputAllRows(writer, rawRows);
        rawRows.clear();
    }

    private static void processMeme(String meme, Map<TimelineContext, Timeline>  timelines) throws IOException, ParseException {
        // COB process()
        CobProcessor.processCob(meme, timelines);

        // Address process()

        // NameProcessor process()
        NameProcessor.processName(meme, timelines);

        // Eligibility process()
    }

    private static void processCob(List<Timeline> cobLines) {
        LocalDate today = new LocalDate();
        LocalDate minDate = today.minus(lookback);
        for(LocalDate day = new LocalDate(); day.isAfter(minDate); day.minusDays(1)) {
            int totalPrimary = 0;
            for(Timeline cob : cobLines) {
                if (StringUtils.equals(ObjectUtils.toString(cob.get(day)), "P")) {
                    totalPrimary++;
                }
            }
            if(totalPrimary > 1) {
                for(Timeline cob: cobLines) {
                    cob.storeVector(day, day, null);
                }
            }
        }
    }

    private static void ouputAllRows(BufferedWriter writer, Deque<Map<TimelineContext, Timeline>> rawRows) {
        // Process the COB accross CTG if needed
        if(rawRows.size() > 1) {
            List<Timeline> cobLines = new ArrayList<Timeline>();
            for(Map<TimelineContext, Timeline> lines : rawRows) {
                cobLines.add(lines.get(TimelineContext.COB));
            }
            processCob(cobLines);
        }

        // Out all the rows
        while(!rawRows.isEmpty()) {
            outputRowTo2A(writer, rawRows.removeFirst());
        }
    }

    private static void outputRowTo2A(BufferedWriter writer, Map<TimelineContext, Timeline> timelines) {

    }
}
