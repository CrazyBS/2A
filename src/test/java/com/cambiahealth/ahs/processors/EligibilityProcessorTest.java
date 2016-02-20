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
        //expectedElig.put()
        expectedElig.put("CTG_ID", "100671251");
        expectedElig.put("PRODUCT_ID", "OMO12345");
        expectedElig.put("MEME_CK", "100671251");
        expectedElig.put("CSPI_ID", "MESA2001");
        expectedElig.put("DOB", "1960-01-01");
        expectedElig.put("PLAN", "851");
        expectedElig.put("MEME_EFFECTIVE_DATE", "2016-02-01");
        expectedElig.put("CSPI_ITS_PREFIX", "PQL");
        expectedElig.put("CSCS_ID", "0003");
        expectedElig.put("MASK_IND", "N");
        expectedElig.put("HOST_PLAN_OVERRIDE", "350");
        expectedElig.put("MEME_TERMINATION_DATE", "2016-02-29");
        expectedElig.put("GENDER", "F");
        expectedElig.put("SBSB_ID", "100000250");
        expectedElig.put("ATTRIBUTION_PARN_IND", "N");
        expectedElig.put("RELATIONSHIP_TO_SUBSCRIBER", "W");

        List<TimeVector> expected = new ArrayList<TimeVector>();
        expected.add(new TimeVector(new LocalDate(2016,2,1), new LocalDate(2016,2,29), expectedElig));

        Assert.assertNotNull(timelines.get(TimelineContext.ELIGIBILITY));

        Assert.assertTrue(testVectorList(expected, timelines.get(TimelineContext.ELIGIBILITY).getTimelineVectors()));
    }

    private boolean testVectorList(List<TimeVector> left, List<TimeVector> right) {
        if (left.size() != right.size()) {
            return false;
        }

        boolean isPass = true;

        for(int i = 0; i < left.size(); i++) {
            isPass = isPass && ObjectUtils.equals(left.get(i), right.get(i));
        }

        return isPass;
    }
}
