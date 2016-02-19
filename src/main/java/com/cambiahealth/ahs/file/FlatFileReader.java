package com.cambiahealth.ahs.file;

import org.apache.commons.lang3.StringUtils;

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
        if(null == line) {
            return null;
        }
        List<String> columnNames = descriptor.getSchema();
        HashMap<String, String> rowData = new HashMap<String, String>(columnNames.size());

        String[] columns = StringUtils.split(line, "|");
        if(columnNames.size() != columns.length) {
            throw new RuntimeException("The number of columns in the file: " +
                    columns.length + " does not match the columns in the descriptor: " +
                    columnNames.size() + " from the descriptor: " + descriptor.name());
        }

        for(int i =0;i<columns.length ;i++) {
            String columnData = columns[i];
            String columnName = columnNames.get(i);

            rowData.put(columnName, columnData);
        }

        return rowData;
    }

    public void unRead() {
        useBuf = true;
    }
}
