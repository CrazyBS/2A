package com.cambiahealth.ahs;

import com.cambiahealth.ahs.timeline.Timeline;
import com.cambiahealth.ahs.timeline.TimelineContext;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.cglib.core.Local;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by r628601 on 2/19/2016.
 */
public class CaneCondenserTest {


    @Before
    public void before() throws FileNotFoundException{

    }

    @Test
    @Ignore
    public void testOutputRowTo2A(){
        Map<TimelineContext,Timeline> myMap = new HashMap<TimelineContext,Timeline>();
        LocalDate today = new LocalDate();
        LocalDate start = today.minusDays(1095);
        Timeline cob = new Timeline();
        Timeline name = new Timeline();
        Timeline primaryAddress = new Timeline();
        Timeline secondaryAddress = new Timeline();
        Timeline eligibility = new Timeline();
        Map<String,String> blankMap = new HashMap<String,String>();
        Map<String,String> tempMap = new HashMap<String, String>();

        for(int i = 0; i<1095; i++){
            if(i%13==0){
                tempMap.put("MEME"+i,i+"is divisible by 13");
                cob.storeVector(start.plusDays(i-13),start.plusDays(i),tempMap);
                tempMap=blankMap;
            }
            if(i%17==0){
                tempMap.put("MEME"+i,i+"is divisible by 17");
                name.storeVector(start.plusDays(i-17),start.plusDays(i),tempMap);
                tempMap=blankMap;
            }
            if(i%19==0){
                tempMap.put("MEME"+i,i+"is divisible by 19");
                primaryAddress.storeVector(start.plusDays(i-19),start.plusDays(i),tempMap);
                tempMap=blankMap;
            }
            if(i%31==0){
                tempMap.put("MEME"+i,i+"is divisible by 31");
                secondaryAddress.storeVector(start.plusDays(i-31),start.plusDays(i),tempMap);
                tempMap=blankMap;
            }
            if(i%57==0){
                tempMap.put("MEME"+i,i+"is divisible by 57");
                eligibility.storeVector(start.plusDays(i-57),start.plusDays(i),tempMap);
                tempMap=blankMap;
            }

        }
        myMap.put(TimelineContext.COB,cob);
        myMap.put(TimelineContext.NAME,name);
        myMap.put(TimelineContext.ADDRESS_PRIMARY,primaryAddress);
        myMap.put(TimelineContext.ADDRESS_SECONDARY,secondaryAddress);
        myMap.put(TimelineContext.ELIGIBILITY,eligibility);

        Main.outputRowTo2A(null,myMap);


    }
}
