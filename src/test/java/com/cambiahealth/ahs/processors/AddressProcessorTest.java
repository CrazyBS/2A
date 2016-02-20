package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileResolverFactory;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import org.junit.After;
import org.junit.Before;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bpyl on 2/19/2016.
 */
public class AddressProcessorTest {

    private static Map<FileDescriptor, String> descriptors = new HashMap<FileDescriptor, String>();
    static {{
        descriptors.put(FileDescriptor.CONFIDENTIAL_ADDRESS_EXTRACT, "OOA_Sub_Address_Extract.dat");
        descriptors.put(FileDescriptor.SUBSCRIBER_ADDRESS_EXTRACT, "OOA_Conf_Address_Extract.dat");
        descriptors.put(FileDescriptor.ZIP_CODE_EXTRACT, "OOA_Zipcode_Extract.dat");
    }}

    private static FlatFileResolverFactory factory = new FlatFileResolverFactory(true);
    private static IFlatFileResolver resolver = factory.getInstance(descriptors);

    @Before
    public void before() throws IOException {
        AddressProcessor.initialize(resolver);
    }

    @After
    public void after() throws IOException {
        AddressProcessor.shutdown();
    }
}
