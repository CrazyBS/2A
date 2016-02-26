package com.cambiahealth.ahs.file;

import com.cambiahealth.ahs.entity.Column;
import com.cambiahealth.ahs.entity.FixedWidth;
import com.cambiahealth.ahs.processors.TransformProcessor;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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

    private enum TestFormat implements FixedWidth {
        ONE(5),
        TWO(15),
        THREE(50);

        private int width;

        TestFormat(int width) {
            this.width = width;
        }

        public int getFixedWidth() {
            return width;
        }
    }

    @Before
    public void before() throws IOException {
        writer = resolver.writeFile(FileDescriptor.FINAL_2A_OUTPUT);
    }

    @Test
    public void testWriteLine() throws IOException {
        LinkedHashMap<TestFormat,String> data = new LinkedHashMap<TestFormat, String>();
        data.put(TestFormat.ONE, "TEST");
        data.put(TestFormat.TWO, "TSET");

        FlatFileWriter.writeLine(data,writer);

        ByteArrayOutputStream bos = ((ResourceFlatFileResolver) resolver).getBos();
        byte[] byteArray = bos.toByteArray();
        //Test line length, content

        Assert.assertEquals("TEST TSET           \n", new String(byteArray));
    }

    @Test
    public void testProcessAddress() throws IOException, ParseException {
        LinkedHashMap<TestFormat,String> data = new LinkedHashMap<TestFormat,String>();

        data.put(TestFormat.ONE, "TEST");

        String result = FlatFileWriter.generateLine(data);

        Assert.assertEquals(5, result.length());

        data.put(TestFormat.TWO, "TEST AGAIN");

        result = FlatFileWriter.generateLine(data);

        Assert.assertEquals(20, result.length());

        data.put(TestFormat.THREE, "TEST YET AGAIN");

        result = FlatFileWriter.generateLine(data);

        Assert.assertEquals(70, result.length());
        Assert.assertEquals("TEST T", result.substring(0,6));
        Assert.assertEquals("YET", result.substring(25,28));
        Assert.assertEquals("     ", result.substring(65,70));
    }

    @After
    public void after() throws IOException {
        writer.flush();
        writer.close();
    }
}
