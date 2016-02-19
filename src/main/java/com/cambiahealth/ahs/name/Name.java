package com.cambiahealth.ahs.name;

import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.LocalDate;

/**
 * Created by msnook on 2/18/2016.
 */
public class Name {
    private enum NAMECOLUMNS {
        MEME_CK("MEME_CK"),
        START_DT("START_DT"),
        END_DT("END_DT"),
        FIRST_NAME("FIRST_NAME"),
        LAST_NAME("LAST_NAME");

        public final String columnName;

        private NAMECOLUMNS(String columnName){
            this.columnName = columnName;
        }
    }

    public static void processAddress(IFlatFileResolver resolver, String MEME, Timeline timeline)
    {
        FlatFileReader reader;
        Map<String, String> storedLine = new HashMap<String, String>();
        LocalDate storedStart = new LocalDate();
        LocalDate storedEnd = new LocalDate();

        try {
            reader = resolver.getFile(FileDescriptor.CONFIDENTIAL_ADDRESS_EXTRACT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        while(true) {
            Map<String, String> line = new HashMap<String, String>();
            try {
                line = reader.readColumn();
            } catch (IOException e) {
                if(!storedLine.isEmpty()){
                    timeline.storeVector(storedStart, storedEnd, storedLine);
                } else{
                    e.printStackTrace();
                }
                break;
            }

            if (line.get(NAMECOLUMNS.MEME_CK) != MEME) {
                reader.unRead();
                timeline.storeVector(storedStart, storedEnd, storedLine);
                break;
            } else {
                if(storedLine.isEmpty()) {
                    storedLine = new HashMap<String,String>(line);
                    storedStart = new LocalDate(line.get(NAMECOLUMNS.START_DT.columnName));
                    storedEnd = new LocalDate(line.get(NAMECOLUMNS.END_DT.columnName));
                } else if(!line.get(NAMECOLUMNS.FIRST_NAME).equals(storedLine.get(NAMECOLUMNS.FIRST_NAME)) ||
                          !line.get(NAMECOLUMNS.LAST_NAME).equals(storedLine.get(NAMECOLUMNS.LAST_NAME))){
                    timeline.storeVector(storedStart, storedEnd, storedLine);
                    storedLine = new HashMap<String,String>(line);
                    storedStart = new LocalDate(line.get(NAMECOLUMNS.START_DT.columnName));
                    storedEnd = new LocalDate(line.get(NAMECOLUMNS.END_DT.columnName));
                } else{
                    storedLine = new HashMap<String,String>(line);
                    storedEnd = new LocalDate(line.get(NAMECOLUMNS.END_DT.columnName));
                }
            }
        }
    }
}
