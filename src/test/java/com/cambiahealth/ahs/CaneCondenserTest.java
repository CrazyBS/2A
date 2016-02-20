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
        today = today.minusDays(today.getDayOfMonth());
        LocalDate start = today.minusDays(1095);
        Timeline cob = new Timeline();
        Timeline name = new Timeline();
        Timeline primaryAddress = new Timeline();
        Timeline secondaryAddress = new Timeline();
        Timeline eligibility = new Timeline();
/*
        for(int i = 0; i<1095; i++){

            if(i%2==0){
                Map<String,String> tempMap = new HashMap<String, String>();
                tempMap.put("MEME "+i,"cob is 2");
                cob.storeVector(start.plusDays(i-2),start.plusDays(i),tempMap);
            }
            if(i%3==0){
                Map<String,String> tempMap = new HashMap<String, String>();
                tempMap.put("MEME "+i,"name is 3");
                name.storeVector(start.plusDays(i-3),start.plusDays(i),tempMap);
            }
            if(i%5==0){
                Map<String,String> tempMap = new HashMap<String, String>();
                tempMap.put("MEME "+i,"primary address is 5");
                primaryAddress.storeVector(start.plusDays(i-5),start.plusDays(i),tempMap);
            }
            if(i%7==0){
                Map<String,String> tempMap = new HashMap<String, String>();
                tempMap.put("MEME "+i," secondary address is 7");
                secondaryAddress.storeVector(start.plusDays(i-7),start.plusDays(i),tempMap);
            }
            if(i%11==0){
                Map<String,String> tempMap = new HashMap<String, String>();
                tempMap.put("MEME "+i,"eligibility is 11");
                eligibility.storeVector(start.plusDays(i-11),start.plusDays(i),tempMap);
            }
        }
*/
        HashMap<String, String> name1 = new HashMap<String, String>();
        name1.put("z", "a");
        name.storeVector(today.minusDays(13),today.minusDays(12),name1);
        HashMap<String, String> name2 = new HashMap<String, String>();
        name2.put("z","b");
        name.storeVector(today.minusDays(8),today.minusDays(6),name2);
        HashMap<String, String> name3 = new HashMap<String, String>();
        name3.put("z","c");
        name.storeVector(today.minusDays(4),today,name3);


        HashMap<String, String> primary1 = new HashMap<String, String>();
        primary1.put("a", "a");
        primaryAddress.storeVector(today.minusDays(15),today.minusDays(12),primary1);
        HashMap<String, String> primary2 = new HashMap<String, String>();
        primary2.put("b","b");
        primaryAddress.storeVector(today.minusDays(9),today.minusDays(2),primary2);
        HashMap<String, String> primary3 = new HashMap<String, String>();
        primary3.put("c","c");
        primaryAddress.storeVector(today.minusDays(1),today,primary3);
        
        HashMap<String, String> cob1 = new HashMap<String, String>();
        cob1.put("y", "a");
        cob.storeVector(today.minusDays(9),today.minusDays(6),cob1);
        HashMap<String, String> cob2 = new HashMap<String, String>();
        cob2.put("y","b");
        cob.storeVector(today.minusDays(5),today.minusDays(3),cob2);
        HashMap<String, String> cob3 = new HashMap<String, String>();
        cob3.put("y","c");
        cob.storeVector(today.minusDays(2),today,cob3);
        
        HashMap<String, String> secondary1 = new HashMap<String, String>();
        secondary1.put("x", "a");
        secondaryAddress.storeVector(today.minusDays(5),today.minusDays(3),secondary1);
        HashMap<String, String> secondary2 = new HashMap<String, String>();
        secondary2.put("x","b");
        secondaryAddress.storeVector(today.minusDays(3),today.minusDays(2),secondary2);
        HashMap<String, String> secondary3 = new HashMap<String, String>();
        secondary3.put("x","c");
        secondaryAddress.storeVector(today.minusDays(1),today,secondary3);

        HashMap<String, String> eligty1 = new HashMap<String, String>();
        eligty1.put("1", "1");
        eligibility.storeVector(today.minusDays(10),today.minusDays(7),eligty1);
        HashMap<String, String> eligty2 = new HashMap<String, String>();
        eligty2.put("2","2");
        eligibility.storeVector(today.minusDays(5),today.minusDays(2),eligty2);
        HashMap<String, String> eligty3 = new HashMap<String, String>();
        eligty3.put("3","3");
        eligibility.storeVector(today.minusDays(1),today,eligty3);
       
        myMap.put(TimelineContext.COB,cob);
        myMap.put(TimelineContext.NAME,name);
        myMap.put(TimelineContext.ADDRESS_PRIMARY,primaryAddress);
        myMap.put(TimelineContext.ADDRESS_SECONDARY,secondaryAddress);
        myMap.put(TimelineContext.ELIGIBILITY,eligibility);

        Main.outputRowTo2A(null, myMap);


    }
}
