package com.cambiahealth.ahs.file;

import com.cambiahealth.ahs.entity.Column;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by msnook on 2/20/2016.
 */
public class FlatFileWriter {

    public static void writeLine(Map<String,Column> data, BufferedWriter writer) throws IOException {
        String line = generateLine(data);

        writer.write(line);
        writer.newLine();
        writer.flush();
    }

    public static String generateLine(Map<String,Column> data){
        String line = "";
        for(Column column : data.values()){
            String columnValue = String.format("%1$-" +  column.getColumnLength() + "s", null != column.getColumnValue() ? column.getColumnValue() : "");
            line += columnValue;
        }
        return line;
    }
}
