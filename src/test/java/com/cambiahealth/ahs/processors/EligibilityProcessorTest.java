package com.cambiahealth.ahs.processors;

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
import java.text.ParseException;
import java.util.HashMap;
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
        EligibilityProcessor.processEligibiltiy("136091253", timelines);
        Map<String, String> expectedAcorElig = new HashMap<String, String>();
        Map<String, String> expectedCspiElig = new HashMap<String, String>();

        // 995999996003366|136091253|2014-09-01|2014-12-31|MCUS1001|0001|F|D|1960-01-01|OMO12345|Y|N|
        // 136091253|MCUS1001|0001|2014-09-01 00:00:00|2014-12-31 00:00:00|PQL|100000300

        expectedAcorElig.put("CTG_ID", "995999996003366");
        expectedAcorElig.put("PRODUCT_ID", "OMO12345");
        expectedAcorElig.put("MEME_CK", "136091253");
        expectedAcorElig.put("CSPI_ID", "MCUS1001");
        expectedAcorElig.put("DOB", "1960-01-01");
        expectedAcorElig.put("MEME_EFFECTIVE_DATE", "2014-09-01");
        expectedAcorElig.put("CSCS_ID", "0001");
        expectedAcorElig.put("MASK_IND", "N");
        expectedAcorElig.put("MEME_TERMINATION_DATE", "2014-12-31");
        expectedAcorElig.put("GENDER", "F");
        expectedAcorElig.put("ATTRIBUTION_PARN_IND", "Y");
        expectedAcorElig.put("RELATIONSHIP_TO_SUBSCRIBER", "D");

        expectedCspiElig.put("CSPI_ITS_PREFIX", "PQL");
        expectedCspiElig.put("PLAN", "851");
        expectedCspiElig.put("CSPI_ID", "MCUS1001");
        expectedCspiElig.put("CSCS_ID", "0001");
        expectedCspiElig.put("MEME_CK", "136091253");
        expectedCspiElig.put("CSPI_EFF_DT", "2014-09-01 00:00:00");
        expectedCspiElig.put("CSPI_TERM_DT", "2014-12-31 00:00:00");
        expectedCspiElig.put("SBSB_ID", "100000300");

        // {CSPI_EFF_DT=2014-09-01 00:00:00, CSPI_ID=MCUS1001, MEME_CK=136091253, PLAN=851, SBSB_ID=100000300, CSPI_ITS_PREFIX=PQL, CSCS_ID=0001, CSPI_TERM_DT=2014-12-31 00:00:00}


        Assert.assertNotNull(timelines.get(TimelineContext.ACORS_ELIGIBILITY));

        Assert.assertEquals(expectedAcorElig, timelines.get(TimelineContext.ACORS_ELIGIBILITY).get(new LocalDate(2014,9,1)));
        Assert.assertEquals(expectedAcorElig, timelines.get(TimelineContext.ACORS_ELIGIBILITY).get(new LocalDate(2014,9,2)));
        Assert.assertEquals(expectedAcorElig, timelines.get(TimelineContext.ACORS_ELIGIBILITY).get(new LocalDate(2014,12,31)));
        Assert.assertEquals(expectedAcorElig, timelines.get(TimelineContext.ACORS_ELIGIBILITY).get(new LocalDate(2014,12,30)));
        Assert.assertNotEquals(expectedAcorElig, timelines.get(TimelineContext.ACORS_ELIGIBILITY).get(new LocalDate(2014,8,31)));
        Assert.assertNotEquals(expectedAcorElig, timelines.get(TimelineContext.ACORS_ELIGIBILITY).get(new LocalDate(2015,1,1)));

        Assert.assertEquals(expectedCspiElig, timelines.get(TimelineContext.CSPI_ELIGIBILITY).get(new LocalDate(2014,9,1)));
        Assert.assertEquals(expectedCspiElig, timelines.get(TimelineContext.CSPI_ELIGIBILITY).get(new LocalDate(2014,9,2)));
        Assert.assertEquals(expectedCspiElig, timelines.get(TimelineContext.CSPI_ELIGIBILITY).get(new LocalDate(2014,12,31)));
        Assert.assertEquals(expectedCspiElig, timelines.get(TimelineContext.CSPI_ELIGIBILITY).get(new LocalDate(2014,12,30)));
        Assert.assertNotEquals(expectedCspiElig, timelines.get(TimelineContext.CSPI_ELIGIBILITY).get(new LocalDate(2014,8,31)));
        Assert.assertNotEquals(expectedCspiElig, timelines.get(TimelineContext.CSPI_ELIGIBILITY).get(new LocalDate(2015,1,1)));
    }

    @Test
    public void testPreservationOfRows() throws IOException, ParseException {
        Map<TimelineContext, Timeline> timelines = new HashMap<TimelineContext, Timeline>();
        /*
            105977752|105977752
            995999996003365|136091254
            995999996003366|136091253
         */
        EligibilityProcessor.processEligibiltiy("105977752", timelines);
        Assert.assertNotNull(timelines.get(TimelineContext.ACORS_ELIGIBILITY));
        Assert.assertFalse(timelines.get(TimelineContext.ACORS_ELIGIBILITY).isEmpty());
        timelines.clear();

        EligibilityProcessor.processEligibiltiy("136091253", timelines);
        Assert.assertNotNull(timelines.get(TimelineContext.ACORS_ELIGIBILITY));
        Assert.assertFalse(timelines.get(TimelineContext.ACORS_ELIGIBILITY).isEmpty());
        timelines.clear();

        EligibilityProcessor.processEligibiltiy("136091254", timelines);
        Assert.assertNotNull(timelines.get(TimelineContext.ACORS_ELIGIBILITY));
        Assert.assertFalse(timelines.get(TimelineContext.ACORS_ELIGIBILITY).isEmpty());
    }

}
