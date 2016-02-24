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
import java.lang.reflect.Member;
import java.text.ParseException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
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

    /**
     * @TODO MUST deal with tracking the columns that change.  Need a new line, when changed.
     *
     *  Needs more refactoring...   Too much duplication
     *
     *
     * @param writer
     * @param timelines
     * @return
     * @throws IOException
     */
    static Timeline outputRowTo2A(BufferedWriter writer, Map<TimelineContext, Timeline> timelines) throws IOException {
        Timeline cob = timelines.get(TimelineContext.COB);
        Timeline name = timelines.get(TimelineContext.NAME);
        Timeline primaryAddress = timelines.get(TimelineContext.ADDRESS_PRIMARY);
        Timeline secondaryAddress = timelines.get(TimelineContext.ADDRESS_SECONDARY);
        Timeline eligibility = timelines.get(TimelineContext.ELIGIBILITY);
        Timeline output = new Timeline();

        LocalDate start = new LocalDate().minusMonths(1).withDayOfMonth(1).minusYears(2);

        LocalDate end = new LocalDate().minusMonths(1).dayOfMonth().withMaximumValue();

        LocalDate curDay = new LocalDate(start);
        LocalDate validDate = null;
        Map<String, String> combinedData = new HashMap<String, String>(45);
        int prevHashCode = 1;
        boolean shouldStartNewRow;
        boolean shouldOutputRow;
        boolean hasRowToOutput = false;
        boolean firstDay = true;
        LocalDate highestStart = new LocalDate();
        LocalDate lowestEnd = new LocalDate();

        for(;curDay.isBefore(end.plusDays(1));) {
            TimeVector primVector = primaryAddress.getVector(curDay);
            TimeVector secdVector = secondaryAddress.getVector(curDay);
            TimeVector nameVector = name.getVector(curDay);
            TimeVector eligVector = eligibility.getVector(curDay);
            TimeVector cobVector  = cob.getVector(curDay);

            if(firstDay) {
                highestStart = getHighest(primVector.getStart(), secdVector.getStart(), nameVector.getStart(), eligVector.getStart(), cobVector.getStart());
            }
            lowestEnd = getLowest(primVector.getEnd(), secdVector.getEnd(), nameVector.getEnd(), eligVector.getEnd(), cobVector.getEnd());

            Map<String, String> primData = primVector.getStoredObject();
            Map<String, String> secdData = secdVector.getStoredObject();
            Map<String, String> nameData = nameVector.getStoredObject();
            Map<String, String> eligData = eligVector.getStoredObject();
            Map<String, String> cobData  = cobVector.getStoredObject();

            // Determine if this date is a valid date
            boolean isValid = (null != primData) && (null != nameData) && (null != eligData);

            boolean isSame;
            if(isValid) {
                // Determine if we have the same set of data from the last row
                int hashCode = getTriggeredHashCode(eligData, nameData, cobData, primData, secdData);
                isSame = prevHashCode == hashCode;
                prevHashCode = hashCode;
            } else {
                isSame = false;
            }

            // Collect the trigger columns
            shouldOutputRow = hasRowToOutput && (!isValid || !isSame);
            shouldStartNewRow = isValid && !isSame;

            if (shouldOutputRow) {
                // Output row
                output.storeVector(validDate, curDay.minusDays(1), combinedData);
                Map<String, Column> row = TransformProcessor.processTransformationForFile(validDate, curDay.minusDays(1), combinedData);
                FlatFileWriter.writeLine(row, writer);

                hasRowToOutput = false;
                combinedData.clear();
            }

            // Just like counting words in a sentence.
            // inValid tells us when we are in a valid timeline
            // isValid tells us we are currently valid
            // Once that changes, we need to change the inValid state
            if(shouldStartNewRow) {
                validDate = firstDay ? highestStart : new LocalDate(curDay);
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

                hasRowToOutput = true;
            }

            firstDay = false;
            curDay = lowestEnd.plusDays(1);
        }

        if (hasRowToOutput) {
            // Output row
            output.storeVector(validDate, lowestEnd, combinedData);
            Map<String, Column> row = TransformProcessor.processTransformationForFile(validDate, lowestEnd, combinedData);
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

    private static int getTriggeredHashCode(Map<String, String> eligData, Map<String, String> nameData, Map<String, String> cobData, Map<String, String> primData, Map<String, String> secdData) {
        int hashCode = 1;

        hashCode = 31 * hashCode + ObjectUtils.hashCode(eligData.get(AcorsEligibility.RELATIONSHIP_TO_SUBSCRIBER.toString()));
        hashCode = 31 * hashCode + ObjectUtils.hashCode(eligData.get(CspiHistory.CSPI_ITS_PREFIX.toString()));
        hashCode = 31 * hashCode + ObjectUtils.hashCode(eligData.get(CspiHistory.SBSB_ID.toString()));
        hashCode = 31 * hashCode + ObjectUtils.hashCode(nameData.get(MemberHistory.MEME_FIRST_NAME.toString()));
        hashCode = 31 * hashCode + ObjectUtils.hashCode(nameData.get(MemberHistory.MEME_LAST_NAME.toString()));
        hashCode = 31 * hashCode + ObjectUtils.hashCode(eligData.get(AcorsEligibility.ATTRIBUTION_PARN_IND.toString()));

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
