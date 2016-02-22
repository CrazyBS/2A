package com.cambiahealth.ahs;

import com.cambiahealth.ahs.entity.*;
import com.cambiahealth.ahs.file.*;
import com.cambiahealth.ahs.processors.*;
import com.cambiahealth.ahs.timeline.TimeVector;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
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

        LocalDate start = new LocalDate().minusMonths(1).withDayOfMonth(1).minusYears(3);
        LocalDate twoYearStart = start.plusYears(1);
        LocalDate end = new LocalDate().minusMonths(1).dayOfMonth().withMaximumValue();

        LocalDate curDay = new LocalDate(start);
        boolean inValid = false;
        LocalDate validDate = null;
        Map<String, String> combinedData = new HashMap<String, String>(45);
        List<String> triggeredData = new ArrayList<String>(30);

        for(;curDay.isBefore(end.plusDays(1)); curDay = curDay.plusDays(1)) {
            Map<String, String> primData = primaryAddress.get(curDay);
            Map<String, String> secdData = secondaryAddress.get(curDay);
            Map<String, String> nameData = name.get(curDay);
            Map<String, String> eligData = eligibility.get(curDay);
            Map<String, String> cobData = cob.get(curDay);

            // Determine if this date is a valid date
            boolean isValid = (null != primData) && (null != nameData) && (null != eligData);
            boolean isSame = false;

            if(isValid) {
                // Determine if we have the same set of data from the last row
                int hashCode = triggeredData.hashCode();
                triggeredData.clear();
                triggeredData.add(eligData.get(AcorsEligibility.RELATIONSHIP_TO_SUBSCRIBER.toString()));
                triggeredData.add(eligData.get(CspiHistory.CSPI_ITS_PREFIX.toString()));
                triggeredData.add(eligData.get(CspiHistory.SBSB_ID.toString()));
                triggeredData.add(nameData.get(MemberHistory.MEME_FIRST_NAME.toString()));
                triggeredData.add(nameData.get(MemberHistory.MEME_LAST_NAME.toString()));
                triggeredData.add(eligData.get(AcorsEligibility.ATTRIBUTION_PARN_IND.toString()));

                if(null != cobData) {
                    triggeredData.add(cobData.get(Cob.COB_VALUE.toString()));
                }

                // Primary Address
                boolean isConf = null != primData.get(ConfidentialAddress.ENAD_ADDR1.toString());
                triggeredData.add(isConf ? primData.get(ConfidentialAddress.ENAD_ADDR1.toString()) : primData.get(SubscriberAddress.SBAD_ADDR1.toString()));
                triggeredData.add(isConf ? primData.get(ConfidentialAddress.ENAD_ADDR2.toString()) : primData.get(SubscriberAddress.SBAD_ADDR2.toString()));
                triggeredData.add(isConf ? primData.get(ConfidentialAddress.ENAD_CITY.toString()) : primData.get(SubscriberAddress.SBAD_CITY.toString()));
                triggeredData.add(isConf ? primData.get(ConfidentialAddress.ENAD_STATE.toString()) : primData.get(SubscriberAddress.SBAD_STATE.toString()));
                triggeredData.add(isConf ? primData.get(ConfidentialAddress.ENAD_ZIP.toString()) : primData.get(SubscriberAddress.SBAD_ZIP.toString()));

                if(null != secdData) {
                    // Secondary Address
                    triggeredData.add(secdData.get("secd_" + SubscriberAddress.SBAD_ADDR1.toString()));
                    triggeredData.add(secdData.get("secd_" + SubscriberAddress.SBAD_ADDR2.toString()));
                    triggeredData.add(secdData.get("secd_" + SubscriberAddress.SBAD_CITY.toString()));
                    triggeredData.add(secdData.get("secd_" + SubscriberAddress.SBAD_STATE.toString()));
                    triggeredData.add(secdData.get("secd_" + SubscriberAddress.SBAD_ZIP.toString()));
                }

                isSame = triggeredData.hashCode() == hashCode;
            } else {
                isSame = false;
            }
            // Collect the trigger columns


            // Just like counting words in a sentence.
            // inValid tells us when we are in a valid timeline
            // isValid tells us we are currently valid
            // Once that changes, we need to change the inValid state
            if(isValid && !inValid) {
                validDate = new LocalDate(curDay);
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

                inValid = true;
            } else if ((!isValid || !isSame) && inValid && twoYearStart.isBefore(curDay)) {
                // Output row
                output.storeVector(validDate, curDay.minusDays(1), combinedData);
                Map<String, Column> row = TransformProcessor.processTransformationForFile(validDate, curDay.minusDays(1), combinedData);
                FlatFileWriter.writeLine(row, writer);

                inValid = false;
            }
        }

        // The final row needs to have a future date if our timeline is valid at the end date
        if(inValid) {
            TimeVector primData = primaryAddress.getVector(curDay);
            TimeVector secdData = secondaryAddress.getVector(curDay);
            TimeVector nameData = name.getVector(curDay);
            TimeVector eligData = eligibility.getVector(curDay);
            TimeVector cobData = cob.getVector(curDay);
            List<LocalDate> dateList = new ArrayList<LocalDate>();
            // Ensure the forever date will be the last date
            dateList.add(new LocalDate(2199,12,31));

            boolean isValid = (null != primData && null != primData.getStoredObject()) && (null != nameData && null != nameData.getStoredObject()) && (null != eligData && null != eligData.getStoredObject());

            if(isValid) {
                combinedData.putAll(primData.getStoredObject());
                dateList.add(primData.getEnd());
                combinedData.putAll(nameData.getStoredObject());
                dateList.add(nameData.getEnd());
                combinedData.putAll(eligData.getStoredObject());
                dateList.add(eligData.getEnd());
                if(null != secdData) {
                    combinedData.putAll(secdData.getStoredObject());
                    dateList.add(secdData.getEnd());
                }
                if(null != cobData) {
                    combinedData.putAll(cobData.getStoredObject());
                    dateList.add(cobData.getEnd());
                }

                // Use the lowest date of all the vectors
                Collections.sort(dateList);

                output.storeVector(validDate, dateList.get(0), combinedData);
                Map<String, Column> row = TransformProcessor.processTransformationForFile(validDate, dateList.get(0), combinedData);
                FlatFileWriter.writeLine(row, writer);
            }
        }

        return output;
    }
}
