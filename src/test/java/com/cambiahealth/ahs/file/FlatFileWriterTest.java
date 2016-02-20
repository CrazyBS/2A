package com.cambiahealth.ahs.file;

import com.cambiahealth.ahs.entity.Column;
import com.cambiahealth.ahs.processors.TransformProcessor;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by msnook on 2/20/2016.
 */
public class FlatFileWriterTest {
    private static Map<FileDescriptor, String> descriptors = new HashMap<FileDescriptor, String>();
    static {{
        descriptors.put(FileDescriptor.FINAL_2A_OUTPUT, "ndw_member");
    }}

    private static FlatFileResolverFactory factory = new FlatFileResolverFactory(true);
    private static IFlatFileResolver resolver = factory.getInstance(descriptors);
    private static BufferedWriter writer;

    @Before
    public void before() throws IOException {
        writer = resolver.writeFile(FileDescriptor.FINAL_2A_OUTPUT);
    }

    @Test
    public void testWriteLine() throws IOException {
        LinkedHashMap<String,Column> data = new LinkedHashMap<String, Column>();
        Column columnOne = new Column("TEST", 5);
        Column columnTwo = new Column("TSET", 15);

        data.put("One", columnOne);
        data.put("Two", columnTwo);

        FlatFileWriter.writeLine(data,writer);

        //Test line length, content

        FlatFileWriter.writeLine(data,writer);

        //Test file length, content
    }

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

    @After
    public void after() throws IOException {
        writer.flush();
        writer.close();
    }
}
