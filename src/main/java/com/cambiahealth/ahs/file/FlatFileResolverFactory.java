package com.cambiahealth.ahs.file;

import java.util.Map;

/**
 * Created by r627021 on 2/18/2016.
 */
public class FlatFileResolverFactory {
    private static IFlatFileResolver instance;
    private static boolean isTest;

    public FlatFileResolverFactory(boolean isTest) {
        FlatFileResolverFactory.isTest = isTest;
    }

    public IFlatFileResolver getInstance(Map<FileDescriptor, String> descriptors) {
        if(isTest) {
            return new ResourceFlatFileResolver(descriptors);
        } else if(null == instance) {
            instance = new FileFlatFileResolver(descriptors);
        }

        return instance;
    }
}
