package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.ConfidentialAddress;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bpyl on 2/19/2016.
 */
public class AddressProcessorTest {

    private static Map<FileDescriptor, String> descriptors = new HashMap<FileDescriptor, String>();
    static {{
        descriptors.put(FileDescriptor.CONFIDENTIAL_ADDRESS_EXTRACT, "OOA_Conf_Address_Extract.dat");
        descriptors.put(FileDescriptor.SUBSCRIBER_ADDRESS_EXTRACT, "OOA_Sub_Address_Extract.dat");
        descriptors.put(FileDescriptor.ZIP_CODE_EXTRACT, "OOA_Zipcode_Extract.dat");
    }}

    private static FlatFileResolverFactory factory = new FlatFileResolverFactory(true);
    private static IFlatFileResolver resolver = factory.getInstance(descriptors);

    @Before
    public void before() throws IOException {
        AddressProcessor.initialize(resolver);
    }

    @Test
    public void testProcessAddress() throws IOException, ParseException {
        Map<TimelineContext, Timeline> timelines = new HashMap<TimelineContext, Timeline>();

        AddressProcessor.processAddress("104835155", timelines);

        Assert.assertNotNull(timelines.get(TimelineContext.ADDRESS_PRIMARY));

        Assert.assertEquals("Confidential Address",timelines.get(TimelineContext.ADDRESS_PRIMARY).get(new LocalDate(2015,1,1)).get(ConfidentialAddress.ENAD_ADDR1.toString()));
    }

    @After
    public void after() throws IOException {
        AddressProcessor.shutdown();
    }
}
