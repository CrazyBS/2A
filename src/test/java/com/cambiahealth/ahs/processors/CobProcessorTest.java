package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.Cob;
import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.FlatFileResolverFactory;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.TimeVector;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.*;

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
    public void before() throws FileNotFoundException {
        CobProcessor.initialize(resolver);
    }

    @After
    public void after() throws IOException {
        CobProcessor.shutdown();
    }

    @Test
    public void testCobProcessing() throws IOException, ParseException, NoSuchFieldException, IllegalAccessException {
        Map<TimelineContext, Timeline> timelines = new HashMap<TimelineContext, Timeline>();
        CobProcessor.processCob("98848702", timelines);
        /*
            98848702|P|2015-04-01|9999-12-31
            98848702|M|2015-02-01|2015-05-31
         */
        Map<String, String> P = Collections.singletonMap(Cob.COB_VALUE.toString(),"P");
        Map<String, String> M = Collections.singletonMap(Cob.COB_VALUE.toString(),"M");

        Timeline timeline = timelines.get(TimelineContext.COB);

        assertTrue(null != timeline);

        Assert.assertEquals(timeline.get(new LocalDate(2015,5,31)), M);
        Assert.assertEquals(timeline.get(new LocalDate(2015,6,1)), P);
        Assert.assertEquals(timeline.get(new LocalDate(2015,2,1)), M);
        Assert.assertEquals(timeline.get(new LocalDate(9999,12,31)), P);
        Assert.assertEquals(timeline.get(new LocalDate(2015,4,30)), M);
    }

    @Test
    public void testPreservationOfRows() throws IOException, ParseException {
        Map<TimelineContext, Timeline> timelines = new HashMap<TimelineContext, Timeline>();
        /*
            104835155|M|2011-01-01|9999-12-31
            104913404|P|2015-04-01|9999-12-31
            105977752|M|2015-02-01|2015-05-31
        */

        CobProcessor.processCob("104835155", timelines);
        Assert.assertNotNull(timelines.get(TimelineContext.COB));
        Assert.assertFalse(timelines.get(TimelineContext.COB).isEmpty());
        timelines.clear();

        CobProcessor.processCob("104913404", timelines);
        Assert.assertNotNull(timelines.get(TimelineContext.COB));
        Assert.assertFalse(timelines.get(TimelineContext.COB).isEmpty());
        timelines.clear();

        CobProcessor.processCob("105977752", timelines);
        Assert.assertNotNull(timelines.get(TimelineContext.COB));
        Assert.assertFalse(timelines.get(TimelineContext.COB).isEmpty());
    }
}
