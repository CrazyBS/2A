package com.cambiahealth.ahs.file;

import com.cambiahealth.ahs.entity.Column;
import com.cambiahealth.ahs.processors.TransformProcessor;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by msnook on 2/20/2016.
 */
public class FlatFileWriterTest {
    @Test
    public void testProcessAddress() throws IOException, ParseException {
        LinkedHashMap<String,Column> data = new LinkedHashMap<String, Column>();
        Column columnOne = new Column("TEST", 5);

        data.put("One", columnOne);

        String result = FlatFileWriter.generateLine(data);

        Assert.assertEquals(5, result.length());

        Column columnTwo = new Column("TEST AGAIN",10);
        data.put("Two", columnTwo);

        result = FlatFileWriter.generateLine(data);

        Assert.assertEquals(15, result.length());

        Column columnThree = new Column("TEST YET AGAIN",50);
        data.put("Three", columnThree);

        result = FlatFileWriter.generateLine(data);

        Assert.assertEquals(65, result.length());
        Assert.assertEquals("TEST T", result.substring(0,6));
        Assert.assertEquals("YET", result.substring(20,23));
        Assert.assertEquals("     ", result.substring(60,65));
    }
}
