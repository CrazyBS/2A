package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.ClaimsConfig;
import com.cambiahealth.ahs.entity.Cob;
import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import org.joda.time.LocalDate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

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

    public static void processEligibiltiy(String meme, Map<TimelineContext, Timeline> timelines) throws IOException, ParseException {
        Timeline timeline = new Timeline();



        timelines.put(TimelineContext.COB, timeline);
    }

    public static void shutdown() throws IOException {
        acorsReader.close();
        acorsReader = null;

        cspiReader.close();
        cspiReader = null;
    }
}
