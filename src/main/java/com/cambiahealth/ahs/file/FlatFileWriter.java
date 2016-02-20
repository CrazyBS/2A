package com.cambiahealth.ahs.file;

import com.cambiahealth.ahs.entity.Column;

import java.io.BufferedWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by msnook on 2/20/2016.
 */
public class FlatFileWriter {

    public static void writeLine(LinkedHashMap<String,Column> data, BufferedWriter writer){

    }

    public static String generateLine(LinkedHashMap<String,Column> data){
        String line = "";
        for(Column column : data.values()){
            String columnValue = String.format("%1$-" +  column.getColumnLength() + "s", column.getColumnValue());
            line += columnValue;
        }
        return line;
    }
}
