package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * Created by bpyl on 2/18/2016.
 */
public class CobProcessor {
    private static FlatFileReader reader;

    public static void initialize(IFlatFileResolver resolver) throws FileNotFoundException {
        reader = resolver.getFile(FileDescriptor.COB_EXTRACT);
    }

    public static void processCob(String meme, Map<TimelineContext, Timeline> timelines) {
        Timeline timeline = new Timeline();

        // Do logic here


        timelines.put(TimelineContext.COB, timeline);
    }

    public static void shutdown() throws IOException {
        reader.close();
        reader = null;
    }
}
