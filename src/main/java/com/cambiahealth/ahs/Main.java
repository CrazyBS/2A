package com.cambiahealth.ahs;

import com.cambiahealth.ahs.entity.*;
import com.cambiahealth.ahs.file.*;
import com.cambiahealth.ahs.processors.*;
import com.cambiahealth.ahs.timeline.TimeVector;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class Main {

    private static int totalMeme = 0;
    private static int rejectedMeme = 0;
    private static int totalOutputtedRows = 0;
    private static int rejectedAfterTimelineReview = 0;
    private static int rejectedQuickly = 0;
    public static Set<String> reportedItsPrefix = new HashSet<String>();

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        String basePath;
        if(args.length < 1) {
            throw new RuntimeException("Must pass base path as first argument");
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

        System.out.println("Total MEME_CK's processed: " + totalMeme);
        System.out.println("Total MEME_CK's rejected: " + rejectedMeme + " (quickly: " + rejectedQuickly + ")(after review: " + rejectedAfterTimelineReview + ")");
        System.out.println("Total MEME_CK's outputted: " + (totalMeme - rejectedMeme));
        System.out.println("Total outputted rows: " + totalOutputtedRows);
    }

    public static void create2A(IFlatFileResolver resolver) throws IOException, ParseException, InterruptedException {
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

    private static void beginProcessing(IFlatFileResolver resolver) throws IOException, ParseException, InterruptedException {
        FlatFileReader reader = resolver.getFile(FileDescriptor.ACORS_ELIGIBILITY_EXTRACT);
        BufferedWriter writer = resolver.writeFile(FileDescriptor.FINAL_2A_OUTPUT);

        String memeCk = null;
        Map<String, String> currentLine = null;
        int i = 1;

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

            totalMeme++;
            if(processMeme(lineMemeCk, timelines)) {
                Timeline output = outputRowsTo2A(writer, timelines);
                if(output.isEmpty()) {
                    rejectedMeme++;
                    rejectedAfterTimelineReview++;
                }
            } else {
                rejectedMeme++;
                rejectedQuickly++;
            }

            // Sleep for a millisecond every 10 rows
            // TODO: Adjust this after tuning on the servers
            // Perhaps make it a parameter
            if(i++ % 10 == 0) {
                Thread.sleep(1);
            }
        }
    }

    private static boolean processMeme(String meme, Map<TimelineContext, Timeline>  timelines) throws IOException, ParseException {
        // Address process()
        Timeline address = AddressProcessor.processAddress(meme, timelines);
        if(address.isEmpty() && address.isEmptyData()) {
            return false;
        }

        // Eligibility process()
        Timeline elig = EligibilityProcessor.processEligibiltiy(meme, timelines);
        if(elig.isEmpty() && elig.isEmptyData()) {
            return false;
        }

        // COB process()
        CobProcessor.processCob(meme, timelines);

        // NameProcessor process()
        NameProcessor.processName(meme, timelines);

        return true;
    }

    /**
     * Walks through all the time vectors and produces changes that occurred in the last 2 years
     * Previous versions walked all 735 days by brute force, this version takes advantage of vectors
     * to jump ahead where possible.
     */
    static Timeline outputRowsTo2A(BufferedWriter writer, Map<TimelineContext, Timeline> timelines) throws IOException {
        Timeline cob = timelines.get(TimelineContext.COB);
        Timeline name = timelines.get(TimelineContext.NAME);
        Timeline primaryAddress = timelines.get(TimelineContext.ADDRESS_PRIMARY);
        Timeline secondaryAddress = timelines.get(TimelineContext.ADDRESS_SECONDARY);
        Timeline eligibility = timelines.get(TimelineContext.ELIGIBILITY);
        Timeline output = new Timeline();

        LocalDate start = new LocalDate().minusMonths(1).withDayOfMonth(1).minusYears(2);
        LocalDate end = new LocalDate().minusMonths(1).dayOfMonth().withMaximumValue();

        LocalDate curDay = new LocalDate(start);
        LocalDate rowStartDate = null;
        Map<String, String> combinedData = new HashMap<String, String>(45);
        int prevHashCode = 1;
        boolean shouldStartNewRow;
        boolean shouldOutputRow;
        boolean hasRowToOutput = false;
        boolean firstDay = true;
        LocalDate highestStart = new LocalDate(1960,1,1);
        LocalDate lowestEnd = new LocalDate(2199,12,31);

        // Loop over the timeline.  curDay will increment based on the earliest next vector
        for(;curDay.isBefore(end.plusDays(1));) {
            TimeVector primVector = primaryAddress.getVector(curDay);
            TimeVector secdVector = secondaryAddress.getVector(curDay);
            TimeVector nameVector = name.getVector(curDay);
            TimeVector eligVector = eligibility.getVector(curDay);
            TimeVector cobVector  = cob.getVector(curDay);

            // We need to determine what our earliest vector date is, so we can set the start date to it
            if(firstDay) {
                highestStart = getHighest(primVector.getStart(), secdVector.getStart(), nameVector.getStart(), eligVector.getStart(), cobVector.getStart());
            }

            // This date represents the earliest we can jump to the next vector
            lowestEnd = getLowest(primVector.getEnd(), secdVector.getEnd(), nameVector.getEnd(), eligVector.getEnd(), cobVector.getEnd());

            // Retrieve our actual map data
            Map<String, String> primData = primVector.getStoredObject();
            Map<String, String> secdData = secdVector.getStoredObject();
            Map<String, String> nameData = nameVector.getStoredObject();
            Map<String, String> eligData = eligVector.getStoredObject();
            Map<String, String> cobData  = cobVector.getStoredObject();

            // Determine if this date is a valid row
            boolean isValid = (null != primData) && (null != nameData) && (null != eligData);

            // We will use isSame to determine if data has changed from one vector to another
            boolean isSame;
            if(isValid) {
                // Determine if we have the same set of data from the last row
                int hashCode = getTriggeredHashCode(eligData, nameData, cobData, primData, secdData);
                isSame = prevHashCode == hashCode;
                prevHashCode = hashCode;
            } else {
                isSame = false;
            }

            // These are the final decisions about whether we are outputting and/or storing a row of data
            shouldOutputRow = hasRowToOutput && (!isValid || !isSame);
            shouldStartNewRow = isValid && !isSame;

            if (shouldOutputRow) {
                // Output row
                totalOutputtedRows++;
                output.storeVector(rowStartDate, curDay.minusDays(1), combinedData);
                Map<String, Column> row = TransformProcessor.processTransformationForFile(rowStartDate, curDay.minusDays(1), combinedData);
                FlatFileWriter.writeLine(row, writer);

                // Reset loop
                hasRowToOutput = false;
                combinedData.clear();
            }

            if(shouldStartNewRow) {
                // If we are the first day, I want to get the earliest date from the vectors
                rowStartDate = firstDay ? highestStart : new LocalDate(curDay);

                // Ensure we indicate that we are now in a valid row of data
                hasRowToOutput = true;
            }

            // Get the most recent data for this row.
            if(isValid) {
                // Combine timeline data
                combinedData.putAll(primData);
                combinedData.putAll(nameData);
                combinedData.putAll(eligData);
                if(null != secdData) {
                    combinedData.putAll(secdData);
                }
                if(null != cobData) {
                    combinedData.putAll(cobData);
                }
            }

            firstDay = false;
            // Jump to one day after the earliest vector end
            curDay = lowestEnd.plusDays(1);
        }

        if (hasRowToOutput) {
            // Output the last row using the largest end date of the current vectors
            output.storeVector(rowStartDate, lowestEnd, combinedData);
            totalOutputtedRows++;
            Map<String, Column> row = TransformProcessor.processTransformationForFile(rowStartDate, lowestEnd, combinedData);
            FlatFileWriter.writeLine(row, writer);
        }

        return output;
    }

    private static LocalDate getLowest(LocalDate ... dates) {
        LocalDate lowestDate = new LocalDate(2199,12,31);
        for(LocalDate date : dates) {
            lowestDate = date.isBefore(lowestDate) ? date : lowestDate;
        }
        return lowestDate;
    }

    private static LocalDate getHighest(LocalDate ... dates) {
        LocalDate highestDate = new LocalDate(1960,1,1);
        for(LocalDate date : dates) {
            highestDate = date.isAfter(highestDate) ? date : highestDate;
        }
        return highestDate;
    }

    /**
     * Using all the columns we have been told should trigger a new row, this method determines the
     * hash code of the data you send it.  Use this to determine if row data has "changed" enough
     * to trigger a new row.
     *
     * Code stolen from the ArrayList::hashCode() method.
     *
     */
    private static int getTriggeredHashCode(Map<String, String> eligData, Map<String, String> nameData, Map<String, String> cobData, Map<String, String> primData, Map<String, String> secdData) {
        int hashCode = 1;

        hashCode = 31 * hashCode + ObjectUtils.hashCode(eligData.get(AcorsEligibility.RELATIONSHIP_TO_SUBSCRIBER.toString()));
        hashCode = 31 * hashCode + ObjectUtils.hashCode(eligData.get(CspiHistory.CSPI_ITS_PREFIX.toString()));
        hashCode = 31 * hashCode + ObjectUtils.hashCode(eligData.get(CspiHistory.SBSB_ID.toString()));
        hashCode = 31 * hashCode + ObjectUtils.hashCode(eligData.get(AcorsEligibility.ATTRIBUTION_PARN_IND.toString()));
        hashCode = 31 * hashCode + ObjectUtils.hashCode(nameData.get(MemberHistory.MEME_FIRST_NAME.toString()));
        hashCode = 31 * hashCode + ObjectUtils.hashCode(nameData.get(MemberHistory.MEME_LAST_NAME.toString()));

        if(null != cobData) {
            hashCode = 31 * hashCode + ObjectUtils.hashCode(cobData.get(Cob.COB_VALUE.toString()));
        }

        // Primary Address
        boolean isConf = null != primData.get(ConfidentialAddress.ENAD_ADDR1.toString());
       hashCode = 31 * hashCode + ObjectUtils.hashCode(isConf ? primData.get(ConfidentialAddress.ENAD_ADDR1.toString()) : primData.get(SubscriberAddress.SBAD_ADDR1.toString()));
       hashCode = 31 * hashCode + ObjectUtils.hashCode(isConf ? primData.get(ConfidentialAddress.ENAD_ADDR2.toString()) : primData.get(SubscriberAddress.SBAD_ADDR2.toString()));
       hashCode = 31 * hashCode + ObjectUtils.hashCode(isConf ? primData.get(ConfidentialAddress.ENAD_CITY.toString()) : primData.get(SubscriberAddress.SBAD_CITY.toString()));
       hashCode = 31 * hashCode + ObjectUtils.hashCode(isConf ? primData.get(ConfidentialAddress.ENAD_STATE.toString()) : primData.get(SubscriberAddress.SBAD_STATE.toString()));
       hashCode = 31 * hashCode + ObjectUtils.hashCode(isConf ? primData.get(ConfidentialAddress.ENAD_ZIP.toString()) : primData.get(SubscriberAddress.SBAD_ZIP.toString()));

        if(null != secdData) {
            // Secondary Address
            hashCode = 31 * hashCode + ObjectUtils.hashCode(secdData.get("secd_" + SubscriberAddress.SBAD_ADDR1.toString()));
            hashCode = 31 * hashCode + ObjectUtils.hashCode(secdData.get("secd_" + SubscriberAddress.SBAD_ADDR2.toString()));
            hashCode = 31 * hashCode + ObjectUtils.hashCode(secdData.get("secd_" + SubscriberAddress.SBAD_CITY.toString()));
            hashCode = 31 * hashCode + ObjectUtils.hashCode(secdData.get("secd_" + SubscriberAddress.SBAD_STATE.toString()));
            hashCode = 31 * hashCode + ObjectUtils.hashCode(secdData.get("secd_" + SubscriberAddress.SBAD_ZIP.toString()));
        }


        return hashCode;
    }
}
