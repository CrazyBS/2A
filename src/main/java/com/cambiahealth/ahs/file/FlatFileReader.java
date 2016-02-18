package com.cambiahealth.ahs.file;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by r627021 on 2/18/2016.
 */
public class FlatFileReader {
    private BufferedReader reader;
    private String bufLine;
    private boolean useBuf;

    public FlatFileReader(BufferedReader reader) {
        this.reader= reader;
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

    public void unRead() {
        useBuf = true;
    }
}
