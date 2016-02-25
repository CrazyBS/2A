package com.cambiahealth.ahs.file;

import com.cambiahealth.ahs.entity.Column;
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

        List<Column> columnNames = descriptor.getSchema();

        if(descriptor.isFixed()) {
            // Read fixed width columns
            return readFixedColumns(line, columnNames);
        } else {
            // Read delimited columns
            return readDelimitedColumns(line, columnNames);
        }
    }

    private Map<String, String> readFixedColumns(String line, List<Column> columnNames) {
        HashMap<String, String> rowData = new HashMap<String, String>(columnNames.size());

        int lineSize = line.length();
        int curStart = 0;

        for(Column column : columnNames) {
            if(lineSize < (curStart + column.getColumnLength())) {
                throw new RuntimeException("The number of characters in the file: " +
                        line.length() + " does not match the fixed width columns in the descriptor: " +
                        columnNames.size() + " from the descriptor: " + descriptor.name() +
                        "\n" + line);
            }
            rowData.put(column.getColumnValue(), StringUtils.trimToNull(StringUtils.substring(line, curStart, curStart + column.getColumnLength())));
            curStart += column.getColumnLength();
        }

        return rowData;
    }

    private Map<String, String> readDelimitedColumns(String line, List<Column> columnNames) {
        HashMap<String, String> rowData = new HashMap<String, String>(columnNames.size());

        String[] columns = StringUtils.splitByWholeSeparatorPreserveAllTokens(line + " ", "|");
        if(columnNames.size() != columns.length) {
            throw new RuntimeException("The number of columns in the file: " +
                    columns.length + " does not match the columns in the descriptor: " +
                    columnNames.size() + " from the descriptor: " + descriptor.name() +
                    "\n" + line);
        }

        for(int i =0;i<columns.length ;i++) {
            String columnData = StringUtils.trimToNull(columns[i]);
            String columnName = columnNames.get(i).getColumnValue();

            if (null == columnData || "\\N".equals(columnData)) {
                continue;
            }

            rowData.put(columnName, columnData);
        }

        return rowData;
    }

    public void unRead() {
        useBuf = true;
    }

    public void close() throws IOException {
        reader.close();
    }
}
