package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.BcbsaMbrPfxSfxXref;
import com.cambiahealth.ahs.entity.MemberHistory;
import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.cambiahealth.ahs.timeline.TimelineContext;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

/**
 * Created by msnook on 2/18/2016.
 */
public class NameProcessor {
    private static FlatFileReader reader;
    private static FlatFileReader fixReader;
    private static Map<String, Map<String, String>> fixes;

    public static void initialize(IFlatFileResolver resolver) throws IOException {
        reader = resolver.getFile(FileDescriptor.MEMBER_HISTORY_EXTRACT);
        fixReader = resolver.getFile(FileDescriptor.BCBSA_MBR_PFX_SFX_XREF);

        fixes = new HashMap<String, Map<String, String>>();

        while(true){
            Map<String, String> line;
            line = fixReader.readColumn(); //TODO Create file
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

    public static void processName(String MEME, Map<TimelineContext, Timeline>  timelines) throws IOException {
        Timeline timeline = new Timeline();
        Map<String, String> storedLine = new HashMap<String, String>();
        LocalDate storedStart = new LocalDate();
        LocalDate storedEnd = new LocalDate();

        while(true) {
            Map<String, String> line;
            line = reader.readColumn();
            line.putAll(new HashMap<String,String>(fixes.get(MEME)));
            if(line != null){
                if (!StringUtils.equals(line.get(MemberHistory.MEME_CK.toString()), MEME)) {
                    if (!storedLine.isEmpty()){
                        reader.unRead();
                        timeline.storeVector(storedStart, storedEnd, storedLine);
                        break;
                    } else if(line.get(MemberHistory.MEME_CK.toString()).compareTo(MEME) > 0){
                        break;
                    }
                } else {
                    if(storedLine.isEmpty()) {
                        storedLine = new HashMap<String,String>(line);
                        storedStart = new LocalDate(line.get(MemberHistory.MEME_EFF_DT.toString()));
                        storedEnd = new LocalDate(line.get(MemberHistory.MEME_TERM_DT.toString()));
                    } else if(!StringUtils.equals(line.get(MemberHistory.MEME_FIRST_NAME.toString()),storedLine.get(MemberHistory.MEME_FIRST_NAME.toString())) ||
                              !StringUtils.equals(line.get(MemberHistory.MEME_LAST_NAME.toString()),storedLine.get(MemberHistory.MEME_LAST_NAME.toString()))   ||
                              !StringUtils.equals(line.get(MemberHistory.MEME_REL.toString()),storedLine.get(MemberHistory.MEME_REL.toString()))){
                        timeline.storeVector(storedStart, storedEnd, storedLine);
                        storedLine = new HashMap<String,String>(line);
                        storedStart = new LocalDate(line.get(MemberHistory.MEME_EFF_DT.toString()));
                        storedEnd = new LocalDate(line.get(MemberHistory.MEME_TERM_DT.toString()));
                    } else{
                        storedLine = new HashMap<String,String>(line);
                        storedEnd = new LocalDate(line.get(MemberHistory.MEME_TERM_DT.toString()));
                    }
                }
            } else {
                if(!storedLine.isEmpty()){
                    timeline.storeVector(storedStart, storedEnd, storedLine);
                }
                break;
            }
        }

        timelines.put(TimelineContext.NAME,timeline);
    }

    public static void shutdown() throws IOException {
        reader.close();
        reader = null;
    }
}
