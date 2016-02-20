package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.BcbsaMbrPfxSfxXref;
import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import org.joda.time.LocalDate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by msnook on 2/19/2016.
 */
public class BcbsaPfxSfxProcessor {
    private static FlatFileReader reader;
    private static Map<String, Map<String, String>> fixes;

    public static void initialize(IFlatFileResolver resolver) throws IOException {
        fixes = new HashMap<String,Map<String,String>>();
        reader = resolver.getFile(FileDescriptor.CONFIDENTIAL_ADDRESS_EXTRACT);

        while(true){
            Map<String, String> line;
            line = reader.readColumn(); //TODO Create file
            if(line == null){
                break;
            } else {
                Map<String, String> lineData = new HashMap<String,String>();
                lineData.put(BcbsaMbrPfxSfxXref.BCBSA_MBR_SFX.toString(),line.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_SFX.toString()));
                lineData.put(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString(),line.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString()));

                fixes.put(line.get(BcbsaMbrPfxSfxXref.MEME_TITLE.toString()), new HashMap<String, String>());
            }
        }
    }

    public static Timeline processFixes(String MEME, Map<TimelineContext, Timeline> timelines){
        Map<String,String> memeFixes = new HashMap<String,String>(fixes.get(MEME));

        Timeline timeline = new Timeline();

        timeline.storeVector(new LocalDate(), new LocalDate(), memeFixes);
        timelines.put(TimelineContext.BCBSA_FIXES, timeline);

        return timeline;
    }

    public static void shutdown() throws IOException {
        reader.close();
        reader = null;

        fixes = null;
    }
}
