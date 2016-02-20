package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.file.FileDescriptor;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bpyl on 2/19/2016.
 */
public class EligibilityProcessorTest {

    private static Map<FileDescriptor, String> descriptors = new HashMap<FileDescriptor, String>();
    static {{
        descriptors.put(FileDescriptor.ACORS_ELIGIBILITY_EXTRACT, "OOA_Acors_Extract.dat");
        descriptors.put(FileDescriptor.CSPI_EXTRACT, "OOA_CSPI_Extract.dat");
        descriptors.put(FileDescriptor.CLAIMS_CONFIG_EXTRACT, "OOA_Claims_Extract.dat");
        descriptors.put(FileDescriptor.CONFIDENTIAL_EMAIL_PHONE_EXTRACT, "OOA_ConfEmailPhone_Extract.dat");
    }}

    private static FlatFileResolverFactory factory = new FlatFileResolverFactory(true);
    private static IFlatFileResolver resolver = factory.getInstance(descriptors);

    @Before
    public void before() throws IOException {
        EligibilityProcessor.initialize(resolver);
    }

    @After
    public void after() throws IOException {
        EligibilityProcessor.shutdown();
    }
    @Test
    public void testEligibilityCase1() throws IOException, ParseException {
        Map<TimelineContext, Timeline> timelines = new HashMap<TimelineContext, Timeline>();
        EligibilityProcessor.processEligibiltiy("995999996003366","136091253", timelines);
        Map<String, String> expectedElig = new HashMap<String, String>();

        // 995999996003366|136091253|2014-09-01|2014-12-31|MCUS1001|0001|F|D|1960-01-01|OMO12345|Y|N|
        // 136091253|MCUS1001|0001|2014-09-01 00:00:00|2014-12-31 00:00:00|PQL|100000300
        expectedElig.put("CTG_ID", "995999996003366");
        expectedElig.put("PRODUCT_ID", "OMO12345");
        expectedElig.put("MEME_CK", "136091253");
        expectedElig.put("CSPI_ID", "MCUS1001");
        expectedElig.put("DOB", "1960-01-01");
        expectedElig.put("PLAN", "851");
        expectedElig.put("MEME_EFFECTIVE_DATE", "2014-09-01");
        expectedElig.put("CSPI_ITS_PREFIX", "PQL");
        expectedElig.put("CSCS_ID", "0001");
        expectedElig.put("MASK_IND", "N");
        expectedElig.put("MEME_TERMINATION_DATE", "2014-12-31");
        expectedElig.put("GENDER", "F");
        expectedElig.put("SBSB_ID", "100000300");
        expectedElig.put("ATTRIBUTION_PARN_IND", "Y");
        expectedElig.put("RELATIONSHIP_TO_SUBSCRIBER", "D");

        Assert.assertNotNull(timelines.get(TimelineContext.ELIGIBILITY));

        Assert.assertEquals(expectedElig, timelines.get(TimelineContext.ELIGIBILITY).get(new LocalDate(2014,9,1)));
        Assert.assertEquals(expectedElig, timelines.get(TimelineContext.ELIGIBILITY).get(new LocalDate(2014,9,2)));
        Assert.assertEquals(expectedElig, timelines.get(TimelineContext.ELIGIBILITY).get(new LocalDate(2014,12,31)));
        Assert.assertEquals(expectedElig, timelines.get(TimelineContext.ELIGIBILITY).get(new LocalDate(2014,12,30)));
        Assert.assertNotEquals(expectedElig, timelines.get(TimelineContext.ELIGIBILITY).get(new LocalDate(2014,8,31)));
        Assert.assertNotEquals(expectedElig, timelines.get(TimelineContext.ELIGIBILITY).get(new LocalDate(2015,1,1)));
    }

}
