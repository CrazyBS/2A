package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.BcbsaMbrPfxSfxXref;
import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileResolverFactory;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by msnook on 2/18/2016.
 */
public class NameProcessorTest {
    private static Map<FileDescriptor, String> descriptors = new HashMap<FileDescriptor, String>();
    static {{
        descriptors.put(FileDescriptor.MEMBER_HISTORY_EXTRACT, "OOA_Member_Extract.dat");
        descriptors.put(FileDescriptor.BCBSA_MBR_PFX_SFX_XREF, "OOA_Title_Extract.dat");
    }}

    private static FlatFileResolverFactory factory = new FlatFileResolverFactory(true);
    private static IFlatFileResolver resolver = factory.getInstance(descriptors);

    @Before
    public void before() throws IOException {
        NameProcessor.initialize(resolver);
    }

    @After
    public void after() throws IOException {
        NameProcessor.shutdown();
    }

    @Test
    public void testProcessName() throws IOException {
        Map<TimelineContext, Timeline> timelines = new HashMap<TimelineContext, Timeline>();

        NameProcessor.processName("100671253", timelines);

        Map<String, String> first = new HashMap<String, String>();
        Map<String, String> last = new HashMap<String, String>();

        /*
            100671253|2014-01-01|2014-12-31||BAR2|FOO||W
            100671253|2015-01-01|2199-12-31||BAR|FOO|F|W
         */

        first.put("MEME_FIRST_NAME", "FOO");
        first.put("MEME_CK", "100671253");
        first.put("MEME_REL", "W");
        first.put("MEME_TERM_DT", "2014-12-31");
        first.put("MEME_LAST_NAME", "BAR2");
        first.put("MEME_EFF_DT", "2014-01-01");
        
        last.put("MEME_FIRST_NAME","FOO");
        last.put("MEME_CK", "100671253");
        last.put("MEME_REL", "W");
        last.put("MEME_MID_INIT", "F");
        last.put("MEME_TERM_DT", "2199-12-31");
        last.put("MEME_LAST_NAME", "BAR");
        last.put("MEME_EFF_DT", "2015-01-01");

        Assert.assertNotNull(timelines.get(TimelineContext.NAME));

        Assert.assertEquals(first, timelines.get(TimelineContext.NAME).get(new LocalDate(2014,1,1)));
        Assert.assertEquals(first, timelines.get(TimelineContext.NAME).get(new LocalDate(2014,12,31)));
        Assert.assertNotEquals(first, timelines.get(TimelineContext.NAME).get(new LocalDate(2013,12,31)));
        Assert.assertNotEquals(first, timelines.get(TimelineContext.NAME).get(new LocalDate(2015,1,1)));
        Assert.assertEquals(last, timelines.get(TimelineContext.NAME).get(new LocalDate(2015,1,1)));
        Assert.assertEquals(last, timelines.get(TimelineContext.NAME).get(new LocalDate(2199,12,31)));
        Assert.assertNotEquals(last, timelines.get(TimelineContext.NAME).get(new LocalDate(2014,12,31)));
    }

    @Test
    public void testProcessRel() throws IOException {
        Map<TimelineContext, Timeline> timelines = new HashMap<TimelineContext, Timeline>();
        NameProcessor.processName("104747002", timelines);

        Map<String, String> first = new HashMap<String, String>();
        Map<String, String> last = new HashMap<String, String>();

        /*
            104747002|2013-01-01|2014-12-31||BAR|FOO|F|W
            104747002|2015-01-01|2199-12-31||BAR|FOO|F|D
         */

        first.put("MEME_FIRST_NAME", "FOO");
        first.put("MEME_CK", "104747002");
        first.put("MEME_REL", "W");
        first.put("MEME_TERM_DT", "2014-12-31");
        first.put("MEME_LAST_NAME", "BAR");
        first.put("MEME_EFF_DT", "2013-01-01");

        last.put("MEME_FIRST_NAME","FOO");
        last.put("MEME_CK", "104747002");
        last.put("MEME_REL", "D");
        last.put("MEME_TERM_DT", "2199-12-31");
        last.put("MEME_LAST_NAME", "BAR");
        last.put("MEME_EFF_DT", "2015-01-01");

        Assert.assertNotNull(timelines.get(TimelineContext.NAME));
        Assert.assertEquals(first, timelines.get(TimelineContext.NAME).get(new LocalDate(2013,1,1)));
        Assert.assertEquals(first, timelines.get(TimelineContext.NAME).get(new LocalDate(2014,12,31)));
        Assert.assertNotEquals(first, timelines.get(TimelineContext.NAME).get(new LocalDate(2012,12,31)));
        Assert.assertNotEquals(first, timelines.get(TimelineContext.NAME).get(new LocalDate(2015,1,1)));
        Assert.assertEquals(last, timelines.get(TimelineContext.NAME).get(new LocalDate(2015,1,1)));
        Assert.assertEquals(last, timelines.get(TimelineContext.NAME).get(new LocalDate(2199,12,31)));
        Assert.assertNotEquals(last, timelines.get(TimelineContext.NAME).get(new LocalDate(2014,12,31)));
    }

    @Test
    public void testPrefixSufficLookup() throws IOException {
        Map<TimelineContext, Timeline> timelines = new HashMap<TimelineContext, Timeline>();
        NameProcessor.processName("100196303", timelines);

        // 100196303|2015-01-01|2199-12-31|DR|BAR|FOO|F|D

        Assert.assertNotNull(timelines.get(TimelineContext.NAME));

        Assert.assertEquals("Dr", timelines.get(TimelineContext.NAME).get(new LocalDate(2015,1,1)).get(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString()));
        Assert.assertNull(timelines.get(TimelineContext.NAME).get(new LocalDate(2015,1,1)).get(BcbsaMbrPfxSfxXref.BCBSA_MBR_SFX.toString()));
    }

    @Test
    public void testRowPreservation() throws IOException {
        Map<TimelineContext, Timeline> timelines = new HashMap<TimelineContext, Timeline>();
/*
        104731206|2015-01-01|2199-12-31||BAR|FOO|F|D
        104747002|2013-01-01|2014-12-31||BAR|FOO||W
        104747002|2015-01-01|2199-12-31||BAR|FOO||D
        104791403|2015-01-01|2199-12-31||BAR|FOO|F|D
*/
        NameProcessor.processName("104731206", timelines);
        Assert.assertNotNull(timelines.get(TimelineContext.NAME));
        Assert.assertFalse(timelines.get(TimelineContext.NAME).isEmpty());
        timelines.clear();

        NameProcessor.processName("104747002", timelines);
        Assert.assertNotNull(timelines.get(TimelineContext.NAME));
        Assert.assertFalse(timelines.get(TimelineContext.NAME).isEmpty());
        timelines.clear();

        NameProcessor.processName("104791403", timelines);
        Assert.assertNotNull(timelines.get(TimelineContext.NAME));
        Assert.assertFalse(timelines.get(TimelineContext.NAME).isEmpty());

    }
}
