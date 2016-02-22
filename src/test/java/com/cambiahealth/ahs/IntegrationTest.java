package com.cambiahealth.ahs;

import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileResolverFactory;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.file.ResourceFlatFileResolver;
import org.junit.Test;

import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bpyl on 2/19/2016.
 */
public class IntegrationTest {

    private static Map<FileDescriptor, String> descriptors = new HashMap<FileDescriptor, String>();
    static {
            descriptors.put(FileDescriptor.ACORS_ELIGIBILITY_EXTRACT, "OOA_Acors_Extract.dat");
            descriptors.put(FileDescriptor.CLAIMS_CONFIG_EXTRACT,  "OOA_Claims_Extract.dat");
            descriptors.put(FileDescriptor.COB_EXTRACT,  "OOA_COB_Extract.dat");
            descriptors.put(FileDescriptor.CONFIDENTIAL_ADDRESS_EXTRACT, "OOA_Conf_Address_Extract.dat");
            descriptors.put(FileDescriptor.CONFIDENTIAL_EMAIL_PHONE_EXTRACT, "OOA_ConfEmailPhone_Extract.dat");
            descriptors.put(FileDescriptor.CSPI_EXTRACT, "OOA_CSPI_Extract.dat");
            descriptors.put(FileDescriptor.MEMBER_HISTORY_EXTRACT, "OOA_Member_Extract.dat");
            descriptors.put(FileDescriptor.SUBSCRIBER_ADDRESS_EXTRACT, "OOA_Sub_Address_Extract.dat");
            descriptors.put(FileDescriptor.ZIP_CODE_EXTRACT, "OOA_Zipcode_Extract.dat");
            descriptors.put(FileDescriptor.BCBSA_MBR_PFX_SFX_XREF, "OOA_Title_Extract.dat");
            descriptors.put(FileDescriptor.FINAL_2A_OUTPUT, "ndw_member");
    }
    FlatFileResolverFactory factory = new FlatFileResolverFactory(true);
    IFlatFileResolver resolver = factory.getInstance(descriptors);

    @Test
    public void testIntegration() throws IOException, ParseException {
        Main.create2A(resolver);

        ByteArrayOutputStream bos = ((ResourceFlatFileResolver) resolver).getBos();
        byte[] data = bos.toByteArray();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));

        // Write your tests against this reader
        reader.readLine();

        reader.close();
    }
}
