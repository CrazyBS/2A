package com.cambiahealth.ahs.processors;

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
public class CobProcessor {
    private static FlatFileReader reader;
    private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public static void initialize(IFlatFileResolver resolver) throws FileNotFoundException {
        reader = resolver.getFile(FileDescriptor.COB_EXTRACT);
    }

    public static void processCob(String meme, Map<TimelineContext, Timeline> timelines) throws IOException, ParseException {
        Timeline timeline = new Timeline();

        while(true) {
            // Do logic here
            Map<String, String> line = reader.readColumn();

            // Are we out of lines?
            if (null == line) {
                break;
            }

            // Get line
            String lineMeme = line.get(Cob.MEME_CK.toString());

            if (null == lineMeme) {
                throw new IOException("Invalid columns");
            }

            int rowTest = lineMeme.compareTo(meme);

            if (rowTest == 0) {
                // We have a match!
                LocalDate start = new LocalDate(format.parse(line.get(Cob.COB_EFFECTIVE_DATE.toString())));
                LocalDate end = new LocalDate(format.parse(line.get(Cob.COB_TERMINATION_DATE.toString())));

                // The data should be sorted MEME_CK, M then P, START_DATE
                Map<String, String> results = new HashMap<String, String>();
                results.put(Cob.COB_VALUE.toString(), line.get(Cob.COB_VALUE.toString()));
                timeline.storeVector(start, end, results);
            } else if (rowTest > 0){
                // We passed it!
                reader.unRead();
                break;
            }
            // Keep looping if we haven't hit, or passed it yet
        }
        timelines.put(TimelineContext.COB, timeline);
    }

    public static void shutdown() throws IOException {
        reader.close();
        reader = null;
    }
}
