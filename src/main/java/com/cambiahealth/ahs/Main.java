package com.cambiahealth.ahs;

import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileResolverFactory;
import com.cambiahealth.ahs.file.IFlatFileResolver;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Map<FileDescriptor, String> descriptors = new HashMap<FileDescriptor, String>();
        descriptors.put(FileDescriptor.ACORS_ELIGIBILITY_EXTRACT, "");
        descriptors.put(FileDescriptor.CLAIMS_CONFIG_EXTRACT, "");
        descriptors.put(FileDescriptor.COB_EXTRACT, "");
        descriptors.put(FileDescriptor.CONFIDENTIAL_ADDRESS_EXTRACT, "");
        descriptors.put(FileDescriptor.CONFIDENTIAL_EMAIL_PHONE_EXTRACT, "");
        descriptors.put(FileDescriptor.CSPI_EXTRACT, "");
        descriptors.put(FileDescriptor.MEMBER_HISTORY_EXTRACT, "");
        descriptors.put(FileDescriptor.SUBSCRIBER_ADDRESS_EXTRACT, "");
        descriptors.put(FileDescriptor.ZIP_CODE_EXTRACT, "");

        FlatFileResolverFactory factory = new FlatFileResolverFactory();
        IFlatFileResolver resolver = factory.getInstance(descriptors);
        // Get next Ctg
        // Loop: Get next meme

        // Process Address (don't forget zip codes)
        // If no remote addresses present, stop processing

        // Merge Eligibility with CSPI History then ClaimConfiguration
        // If no data present, stop processing
        // Store 4 "most recent" columns during this walk.

        // Process COB

        // Reduce Member History

        // Final Merging
        // Store to processed rows buffer

        // End loop condition (no more meme's in CTG set)

        // If more than one processed row pending, then
        // Post process COB

        // Flush processed rows to flat file.

        // Loop Ctg
        System.out.println("I'm a java JAR!");
    }
}
