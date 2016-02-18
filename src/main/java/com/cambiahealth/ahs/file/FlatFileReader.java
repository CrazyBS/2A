package com.cambiahealth.ahs.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by r627021 on 2/18/2016.
 */
public class FlatFileReader {
    private BufferedReader reader;
    private FileDescriptor descriptor;
    private String bufLine;
    private boolean useBuf;

    public FlatFileReader(BufferedReader reader, FileDescriptor descriptor) {
        this.reader= reader;
        this.descriptor = descriptor;
    }

    public String readLine() throws IOException {
        if(useBuf) {
            useBuf = false;
            return bufLine;
        } else {
            bufLine = reader.readLine();
            return bufLine;
        }
    }

    public Map<String, String> readColumn() throws IOException {
        String line = readLine();
        List<String> columns = descriptor.getSchema();
        HashMap<String, String> rowData = new HashMap<String, String>(columns.size());

        return rowData;
    }

    public void unRead() {
        useBuf = true;
    }
}
