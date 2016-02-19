package com.cambiahealth.ahs.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by r627021 on 2/18/2016.
 */
public interface IFlatFileResolver {
    FlatFileReader getFile(FileDescriptor descriptor) throws FileNotFoundException;
    BufferedWriter writeFile(FileDescriptor descriptor) throws IOException;
}
