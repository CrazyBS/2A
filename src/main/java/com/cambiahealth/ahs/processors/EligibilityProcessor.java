package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.AcorsEligibility;
import com.cambiahealth.ahs.entity.ClaimsConfig;
import com.cambiahealth.ahs.entity.Cob;
import com.cambiahealth.ahs.entity.CspiHistory;
import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by bpyl on 2/18/2016.
 */
public class EligibilityProcessor {
    private static FlatFileReader acorsReader;
    private static FlatFileReader cspiReader;
    private static Map<String, String> claimConfig = new HashMap<String, String>();

    private static DateFormat format = new SimpleDateFormat("YYYY-mm-DD");

    public static void initialize(IFlatFileResolver resolver) throws IOException {
        acorsReader = resolver.getFile(FileDescriptor.ACORS_ELIGIBILITY_EXTRACT);
        cspiReader = resolver.getFile(FileDescriptor.CSPI_EXTRACT);

        FlatFileReader reader = resolver.getFile(FileDescriptor.CLAIMS_CONFIG_EXTRACT);
        Map<String, String> row;
        while(null != (row = reader.readColumn())) {
            claimConfig.put(row.get(ClaimsConfig.ALPHA_PREFIX.toString()), row.get(ClaimsConfig.PLAN.toString()));
        }
        reader.close();
    }

    public static Timeline processEligibiltiy(String meme, Map<TimelineContext, Timeline> timelines) throws IOException, ParseException {
        Timeline timeline = new Timeline();

        // We'll use buffers to make the logic easier to manage
        // It could be done by traversing, but we should be okay.
        Deque<Map<String, String>> acorsLines = new ArrayDeque<Map<String, String>>();
        List<Map<String, String>> cspiLines = new ArrayList<Map<String, String>>();

        collectLines(acorsReader, meme, AcorsEligibility.MEME_CK.toString(), acorsLines);

        if(!acorsLines.isEmpty()) {
            // Scroll both reader to the current meme under test
            collectLines(cspiReader, meme, CspiHistory.MEME_CK.toString(), cspiLines);

            // Okay, now we have all of our data, let's try joining them together
            for(Map<String, String> acorsLine : acorsLines) {
                AcorsToCspiKey key = new AcorsToCspiKey(true, acorsLine);

                for(Map<String, String> cspiLine: cspiLines) {
                    AcorsToCspiKey cspiKey = new AcorsToCspiKey(false, cspiLine);

                    if(key.equals(cspiKey)) {
                        // We match, let's look for date match
                        DateTime acorStart = new DateTime(format.parse(acorsLine.get(AcorsEligibility.MEME_EFFECTIVE_DATE.toString())));
                        DateTime cspiStart = new DateTime(format.parse(cspiLine.get(CspiHistory.CSPI_EFF_DT.toString())));
                        DateTime cspiEnd = new DateTime(format.parse(cspiLine.get(CspiHistory.CSPI_TERM_DT.toString())));

                        if(new Interval(cspiStart, cspiEnd).contains(acorStart)) {
                            // We have a complete match!
                            // Collect the rest of the data

                        }
                    }
                }
            }
        }

        // If this returns an empty timeline, we can cancel the rest of the processing on this row
        timelines.put(TimelineContext.COB, timeline);
        return timeline;
    }

    public static void shutdown() throws IOException {
        acorsReader.close();
        acorsReader = null;

        cspiReader.close();
        cspiReader = null;
    }

    private static boolean collectLines(FlatFileReader reader, String meme, String memeColumnName, Collection<Map<String, String>> collection) throws IOException {
        Map<String, String> line;
        while(null != (line = reader.readColumn())) {
            int rowTest = meme.compareTo(line.get(memeColumnName));

            if (rowTest > 0) {
                // We passed it!
                reader.unRead();
                break;
            } else if (rowTest == 0){
                // This is it!
                collection.add(line);
            }
            // We aren't there yet, keep looping
        }

        return !collection.isEmpty();
    }

    private static class AcorsToCspiKey {
        private String meme;
        private String cspi;
        private String cscs;

        public AcorsToCspiKey(String meme, String cspi, String cscs) {
            this.meme = meme;
            this.cspi = cspi;
            this.cscs = cscs;
        }

        public AcorsToCspiKey(boolean isAcors, Map<String, String> map) {
            if(isAcors) {
                this.meme = map.get(AcorsEligibility.MEME_CK.toString());
                this.cspi = map.get(AcorsEligibility.CSPI_ID.toString());
                this.cscs = map.get(AcorsEligibility.CSCS_ID.toString());
            } else {
                this.meme = map.get(CspiHistory.MEME_CK.toString());
                this.cspi = map.get(CspiHistory.CSPI_ID.toString());
                this.cscs = map.get(CspiHistory.CSCS_ID.toString());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AcorsToCspiKey that = (AcorsToCspiKey) o;

            if (!meme.equals(that.meme)) return false;
            if (cspi != null ? !cspi.equals(that.cspi) : that.cspi != null) return false;
            return cscs != null ? cscs.equals(that.cscs) : that.cscs == null;

        }

        @Override
        public int hashCode() {
            int result = meme.hashCode();
            result = 31 * result + (cspi != null ? cspi.hashCode() : 0);
            result = 31 * result + (cscs != null ? cscs.hashCode() : 0);
            return result;
        }

        public String getMeme() {
            return meme;
        }

        public void setMeme(String meme) {
            this.meme = meme;
        }

        public String getCspi() {
            return cspi;
        }

        public void setCspi(String cspi) {
            this.cspi = cspi;
        }

        public String getCscs() {
            return cscs;
        }

        public void setCscs(String cscs) {
            this.cscs = cscs;
        }
    }
}
