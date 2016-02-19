package com.cambiahealth.ahs;

import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.FlatFileResolverFactory;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by msnook on 2/18/2016.
 */
public class Name {
    private enum NAMECOLUMNS {
        MEME_CK, START_DT, END_DT, FIRST_NAME, LAST_NAME
    }

    private enum FILES {
        COB, ADDRESS, NAME, ELIGTY
    }

    private class NameLine {
        public NameLine(){

        }
    }

    public static void processAddress(IFlatFileResolver resolver, String MEME, Timeline timeline)
    {
        FlatFileReader reader;
        Map<String, String> storedLine = new HashMap<String, String>();

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
                    timeline.storeVector(storedLine.get(NAMECOLUMNS.START_DT),storedLine.get(NAMEDCOLUMNS.END_DT), storedLine);
                } else{
                    e.printStackTrace();
                }
                break;
            }

            if (line.get(NAMECOLUMNS.MEME_CK) != MEME) {
                reader.unRead();
                timeline.storeVector(storedLine.get(NAMECOLUMNS.START_DT),storedLine.get(NAMEDCOLUMNS.END_DT), storedLine);
                break;
            } else {
                if(storedLine.isEmpty()) {
                    storedLine = new HashMap<String,String>(line);
                } else if(!line.get(NAMECOLUMNS.FIRST_NAME).equals(storedLine.get(NAMECOLUMNS.FIRST_NAME)) || !line.get(NAMECOLUMNS.LAST_NAME).equals(storedLine.get(NAMECOLUMNS.LAST_NAME))){
                    timeline.storeVector(storedLine.get(NAMECOLUMNS.START_DT),storedLine.get(NAMEDCOLUMNS.END_DT), storedLine);
                    storedLine = new HashMap<String,String>(line);
                } else{
                    storedLine.put(NAMECOLUMNS.END_DT,line.get(NAMECOLUMNS.END_DT));
                }
            }
        }
    }
}
