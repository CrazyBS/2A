package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileResolverFactory;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
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
    }}

    private static FlatFileResolverFactory factory = new FlatFileResolverFactory(true);
    private static IFlatFileResolver resolver = factory.getInstance(descriptors);

    @Before
    public void before() throws FileNotFoundException {
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

        Assert.assertNotNull(timelines.get(TimelineContext.NAME));
    }
}
