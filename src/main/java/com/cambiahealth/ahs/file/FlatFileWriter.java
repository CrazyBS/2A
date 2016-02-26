package com.cambiahealth.ahs.file;

import com.cambiahealth.ahs.entity.Column;
import com.cambiahealth.ahs.entity.FixedWidth;
import com.cambiahealth.ahs.entity.NdwMember;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by msnook on 2/20/2016.
 */
public class FlatFileWriter {

    public static void writeLine(Map<? extends FixedWidth,String> data, BufferedWriter writer) throws IOException {
        String line = generateLine(data);

        writer.write(line);
        writer.write('\n');
        writer.flush();
    }

    public static String generateLine(Map<? extends FixedWidth,String> data){
        String line = "";
        for(FixedWidth dataKey : data.keySet()){
            String dataValue = data.get(dataKey);
            String columnValue = String.format("%1$-" +  dataKey.getFixedWidth() + "s", null != dataValue ? dataValue : "");
            line += columnValue;
        }
        return line;
    }
}
