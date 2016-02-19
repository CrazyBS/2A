package com.cambiahealth.ahs.processors;

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

    public static void initialize(IFlatFileResolver resolver) throws FileNotFoundException {
        reader = resolver.getFile(FileDescriptor.MEMBER_HISTORY_EXTRACT);
    }

    public static void processName(String MEME, Map<TimelineContext, Timeline>  timelines) throws IOException {
        Timeline timeline = new Timeline();
        Map<String, String> storedLine = new HashMap<String, String>();
        LocalDate storedStart = new LocalDate();
        LocalDate storedEnd = new LocalDate();

        while(true) {
            Map<String, String> line;
            line = reader.readColumn();

            if(line != null){
                if (!StringUtils.equals(line.get(MemberHistory.MEME_CK), MEME)) {
                    if (!storedLine.isEmpty()){
                        reader.unRead();
                        timeline.storeVector(storedStart, storedEnd, storedLine);
                        break;
                    } else if(line.get(MemberHistory.MEME_CK).compareTo(MEME) > 0){
                        break;
                    }
                } else {
                    if(storedLine.isEmpty()) {
                        storedLine = new HashMap<String,String>(line);
                        storedStart = new LocalDate(line.get(MemberHistory.MEME_EFF_DT));
                        storedEnd = new LocalDate(line.get(MemberHistory.MEME_TERM_DT));
                    } else if(!StringUtils.equals(line.get(MemberHistory.MEME_FIRST_NAME),storedLine.get(MemberHistory.MEME_FIRST_NAME)) ||
                              !StringUtils.equals(line.get(MemberHistory.MEME_LAST_NAME),storedLine.get(MemberHistory.MEME_LAST_NAME))   ||
                              !StringUtils.equals(line.get(MemberHistory.MEME_REL),storedLine.get(MemberHistory.MEME_REL))){
                        timeline.storeVector(storedStart, storedEnd, storedLine);
                        storedLine = new HashMap<String,String>(line);
                        storedStart = new LocalDate(line.get(MemberHistory.MEME_EFF_DT));
                        storedEnd = new LocalDate(line.get(MemberHistory.MEME_TERM_DT));
                    } else{
                        storedLine = new HashMap<String,String>(line);
                        storedEnd = new LocalDate(line.get(MemberHistory.MEME_TERM_DT));
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
