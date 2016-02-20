package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.ConfidentialAddress;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by msnook on 2/19/2016.
 */
public class TransformProcessorTest {

    @Test
    public void testProcessAddress() throws IOException, ParseException {
        Map<String,String> data = new HashMap<String, String>();
        LocalDate start = new LocalDate();
        LocalDate end = new LocalDate();
        Map<String,String> result;

        result = new HashMap<String,String>(TransformProcessor.processTransformationForFile(start, end, data));

        Assert.assertEquals("GROUP", result.get("Group_or_Individual_Code"));
    }
}
