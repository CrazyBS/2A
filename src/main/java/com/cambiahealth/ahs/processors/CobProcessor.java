package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.Cob;
import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Created by bpyl on 2/18/2016.
 */
public class CobProcessor {
    private static FlatFileReader reader;
    private static DateFormat format = new SimpleDateFormat("")

    public static void initialize(IFlatFileResolver resolver) throws FileNotFoundException {
        reader = resolver.getFile(FileDescriptor.COB_EXTRACT);
    }

    public static void processCob(String meme, Map<TimelineContext, Timeline> timelines) throws IOException {
        Timeline timeline = new Timeline();

        while(true) {

            // Do logic here
            Map<String, String> line = reader.readColumn();

            if (null == line) {
                break;
            }

            String lineMeme = line.get(Cob.MEME_CK.toString());
            if (null == lineMeme) {
                throw new IOException("Invalid columns");
            }

            int rowTest = lineMeme.compareTo(meme);

            if (rowTest < 0) {
                continue;
            } else if (rowTest == 0) {
                // We have a match!

                //timeline.storeVector()
            } else {
                reader.unRead();
                break;
            }
        }
        timelines.put(TimelineContext.COB, timeline);
    }

    public static void shutdown() throws IOException {
        reader.close();
        reader = null;
    }
}
