package com.cambiahealth.ahs.processors;

import com.cambiahealth.ahs.entity.BcbsaMbrPfxSfxXref;
import com.cambiahealth.ahs.entity.MemberHistory;
import com.cambiahealth.ahs.file.FileDescriptor;
import com.cambiahealth.ahs.file.FlatFileReader;
import com.cambiahealth.ahs.file.IFlatFileResolver;
import com.cambiahealth.ahs.timeline.Timeline;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.cambiahealth.ahs.timeline.TimelineContext;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

public class NameProcessor {
    private static FlatFileReader reader;
    private static Map<String, Map<String, String>> fixes;
    private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public static void initialize(IFlatFileResolver resolver) throws IOException {
        reader = resolver.getFile(FileDescriptor.MEMBER_HISTORY_EXTRACT);
        FlatFileReader fixReader = resolver.getFile(FileDescriptor.BCBSA_MBR_PFX_SFX_XREF);

        fixes = new HashMap<String, Map<String, String>>();

        while(true){
            Map<String, String> line;
            line = fixReader.readColumn();
            if(line == null){
                break;
            } else {
                Map<String, String> lineData = new HashMap<String,String>();
                lineData.put(BcbsaMbrPfxSfxXref.BCBSA_MBR_SFX.toString(),line.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_SFX.toString()));
                lineData.put(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString(),line.get(BcbsaMbrPfxSfxXref.BCBSA_MBR_PFX.toString()));

                fixes.put(line.get(BcbsaMbrPfxSfxXref.MEME_TITLE.toString()), lineData);
            }
        }
    }

    public static void processName(String memeCk, Map<TimelineContext, Timeline>  timelines) throws IOException, ParseException {
        Timeline timeline = new Timeline();
        Map<String, String> storedLine;

        while(true) {
            Map<String, String> line = reader.readColumn();

            // Are we out of lines?
            if (null == line) {
                break;
            }

            // Get line
            String lineMeme = line.get(MemberHistory.MEME_CK.toString());

            if (null == lineMeme) {
                throw new IOException("Invalid columns");
            }

            int rowTest = lineMeme.compareTo(memeCk);

            if (rowTest == 0) {
                // We have a match!
                LocalDate start = new LocalDate(format.parse(line.get(MemberHistory.MEME_EFF_DT.toString())));
                LocalDate end = new LocalDate(format.parse(line.get(MemberHistory.MEME_TERM_DT.toString())));
                storedLine = new HashMap<String, String>(line);

                // Deal with last names that have a
                storedLine.put(MemberHistory.MEME_LAST_NAME.toString(), StringUtils.substringBefore(line.get(MemberHistory.MEME_LAST_NAME.toString()),","));
                String endOfName = StringUtils.trimToNull(StringUtils.substringAfterLast(line.get(MemberHistory.MEME_LAST_NAME.toString()),","));
                String memberTitle = line.get(MemberHistory.MEME_TITLE.toString());

                // Check for a title that we can process
                String key = memberTitle == null ? endOfName : memberTitle;
                if(null != key) {
                    Map<String, String> values = fixes.get(key);
                    if(null != values) {
                        storedLine.putAll(values);
                    }
                }

                timeline.storeVector(start, end, storedLine);
            } else if (rowTest > 0){
                // We passed it!
                reader.unRead();
                break;
            }
            // Keep looping if we haven't hit, or passed it yet
        }

        timelines.put(TimelineContext.NAME,timeline);
    }

    public static void shutdown() throws IOException {
        reader.close();
        reader = null;
    }
}
