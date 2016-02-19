package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.Cob;
import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.FlatFileResolverFactory;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by bpyl on 2/18/2016.
 */
public class CobProcessorTest {

    private static Map<FileDescriptor, String> descriptors = new HashMap<FileDescriptor, String>();
    static {{
        descriptors.put(FileDescriptor.COB_EXTRACT, "OOA_COB_Extract.dat");
    }}

    private static FlatFileResolverFactory factory = new FlatFileResolverFactory(true);
    private static IFlatFileResolver resolver = factory.getInstance(descriptors);

    @Before
    public void before() {
        try {
            CobProcessor.initialize(resolver);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @After
    public void after() {
        try {
            CobProcessor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCobProcessing() throws IOException, ParseException {
        Map<TimelineContext, Timeline> timelines = new HashMap<TimelineContext, Timeline>();
        CobProcessor.processCob("98848702", timelines);

        assertTrue(null != timelines.get(TimelineContext.COB));
    }
}
