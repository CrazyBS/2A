package com.cambiahealth.ahs.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created by bpyl on 2/18/2016.
 */
public class ResourceFlatFileResolver implements IFlatFileResolver {
    private Map<FileDescriptor, String> descriptors;

    public ResourceFlatFileResolver(Map<FileDescriptor, String> descriptors) {
        this.descriptors = descriptors;
    }

    public FlatFileReader getFile(FileDescriptor descriptor) throws FileNotFoundException {
        return new FlatFileReader(new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + descriptors.get(descriptor)))), descriptor);
    }

    public Map<FileDescriptor, String> getDescriptors() {
        return descriptors;
    }
}
