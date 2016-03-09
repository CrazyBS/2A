package com.cambiahealth.ahs.file;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bpyl on 2/18/2016.
 */
public class ResourceFlatFileResolver implements IFlatFileResolver {
    private Map<FileDescriptor, String> descriptors;
    private Map<FileDescriptor, ByteArrayOutputStream> bos = new HashMap<FileDescriptor, ByteArrayOutputStream>();

    public ResourceFlatFileResolver(Map<FileDescriptor, String> descriptors) {
        this.descriptors = descriptors;
    }

    public FlatFileReader getFile(FileDescriptor descriptor) throws FileNotFoundException {
        return new FlatFileReader(new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + descriptors.get(descriptor)))), descriptor);
    }

    public BufferedWriter writeFile(FileDescriptor descriptor) throws IOException {
        if(!bos.containsKey(descriptor)) {
            bos.put(descriptor, new ByteArrayOutputStream());
        }
        return new BufferedWriter(new OutputStreamWriter(bos.get(descriptor)));
    }

    public Map<FileDescriptor, String> getDescriptors() {
        return descriptors;
    }

    public ByteArrayOutputStream getBos(FileDescriptor descriptor) {
        return bos.get(descriptor);
    }
}
