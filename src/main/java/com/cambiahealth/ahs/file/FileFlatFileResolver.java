package com.cambiahealth.ahs.file;

import java.io.*;
import java.util.Map;

/**
 * Created by r627021 on 2/18/2016.
 */
public class FileFlatFileResolver implements IFlatFileResolver {
    private Map<FileDescriptor, String> descriptors;

    public FileFlatFileResolver(Map<FileDescriptor, String> descriptors) {
        this.descriptors = descriptors;
    }

    public FlatFileReader getFile(FileDescriptor descriptor) throws FileNotFoundException {
        return new FlatFileReader(new BufferedReader(new FileReader(descriptors.get(descriptor))), descriptor);
    }

    public BufferedWriter writeFile(FileDescriptor descriptor) throws IOException {
        return new BufferedWriter(new FileWriter(descriptors.get(descriptor)));
    }

    public Map<FileDescriptor, String> getDescriptors() {
        return descriptors;
    }
}
