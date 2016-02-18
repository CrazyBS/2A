package com.cambiahealth.ahs.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;

/**
 * Created by r627021 on 2/18/2016.
 */
public interface IFlatFileResolver {
    BufferedReader getFile(FileDescriptor descriptor) throws FileNotFoundException;
}
