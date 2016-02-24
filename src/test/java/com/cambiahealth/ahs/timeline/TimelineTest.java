package com.cambiahealth.ahs.timeline;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by bpyl on 2/19/2016.
 */
public class TimelineTest {

    Map<String, String> data = Collections.singletonMap("foo", "bar");
    Map<String, String> otherData = Collections.singletonMap("foo", "notbar");

    @Test
    public void testEmptyTimeline() {
        Timeline test = new Timeline();

        Assert.assertTrue(test.isEmpty());
    }

    @Test
    public void testNonEmptyTimeline() {
        Timeline test = new Timeline();
        test.storeVector(new LocalDate(2015,1,1), new LocalDate(2015,12,31), data);

        Assert.assertFalse(test.isEmpty());
    }

    @Test
    public void testSingleVector() {
        Timeline test =  new Timeline();
        test.storeVector(new LocalDate(2015,1,1), new LocalDate(2015,12,31), data);

        Assert.assertEquals(data, test.get(new LocalDate(2015,1,1)));
        Assert.assertEquals(data, test.get(new LocalDate(2015,1,2)));
        Assert.assertEquals(data, test.get(new LocalDate(2015,12,31)));
        Assert.assertEquals(data, test.get(new LocalDate(2015,12,30)));
        Assert.assertNotEquals(data, test.get(new LocalDate(2014,12,31)));
        Assert.assertNotEquals(data, test.get(new LocalDate(2016,1,1)));
    }

    @Test
    public void testOverlappingVector() {
        Timeline test =  new Timeline();
        test.storeVector(new LocalDate(2015,1,1), new LocalDate(2015,12,31), data);
        test.storeVector(new LocalDate(2015,7,1), new LocalDate(2016,7,31), otherData);

        Assert.assertEquals(data, test.get(new LocalDate(2015,1,1)));
        Assert.assertEquals(data, test.get(new LocalDate(2015,1,2)));
        Assert.assertEquals(data, test.get(new LocalDate(2015,6,30)));
        Assert.assertEquals(otherData, test.get(new LocalDate(2015,7,1)));
        Assert.assertEquals(otherData, test.get(new LocalDate(2016,7,31)));
        Assert.assertNotEquals(data, test.get(new LocalDate(2014,12,31)));
        Assert.assertEquals(otherData, test.get(new LocalDate(2015,12,31)));
        Assert.assertNotEquals(otherData, test.get(new LocalDate(2016,8,1)));
    }

    @Test
    public void testConsistentData() {
        Timeline test = new Timeline();
        test.addConsistentData("consistent", "data");

        test.storeVector(new LocalDate(2015,1,1), new LocalDate(2015,12,31), data);

        Assert.assertEquals(test.get(new LocalDate(2015,7,1)).get("consistent"), "data");
    }

    @Test
    public void testGetVector() {
        Timeline test = new Timeline();

        test.storeVector(new LocalDate(2015,1,1), new LocalDate(2015,12,31), data);

        TimeVector vector = test.getVector(new LocalDate(2015,7,1));

        Assert.assertNotNull(vector);
        Assert.assertTrue(vector.getStart().equals(new LocalDate(2015,1,1)));
        Assert.assertTrue(vector.getEnd().equals(new LocalDate(2015,12,31)));
        Assert.assertEquals(vector.getStoredObject(), data);
    }

    @Test
    public void testVectorRanges() {
        Timeline test = new Timeline();

        test.storeVector(new LocalDate(2015,1,1), new LocalDate(2015,12,31), data);
        test.storeVector(new LocalDate(2014,7,1), new LocalDate(2015,4,30), otherData);
        test.storeVector(new LocalDate(2015,9,1), new LocalDate(2016,6,30), otherData);

        TimeVector vector = test.getVector(new LocalDate(2015, 7, 1));

        Assert.assertNotNull(vector);
        Assert.assertTrue(vector.getStart().equals(new LocalDate(2015,5,1)));
        Assert.assertTrue(vector.getEnd().equals(new LocalDate(2015,8,31)));
        Assert.assertEquals(vector.getStoredObject(), data);
    }

    @Test
    public void testDependency() {
        Timeline test = new Timeline();
        Timeline dep = new Timeline();

        dep.storeVector(new LocalDate(2015,1,1), new LocalDate(2015,12,31), data);
        test.storeVector(new LocalDate(2015,7,1), new LocalDate(2016,7,1), otherData, dep);

        Assert.assertNull(test.get(new LocalDate(2015,1,1)));
        Assert.assertNull(test.get(new LocalDate(2016,1,1)));
        Assert.assertEquals(test.get(new LocalDate(2015,12,31)), otherData);
    }

    @Test
    public void testGetVectorInvalidRange() {
        Timeline test = new Timeline();

        test.storeVector(new LocalDate(2015,1,1), new LocalDate(2015,12,31), data);
        test.storeVector(new LocalDate(2015,7,1), new LocalDate(2016,7,31), otherData);

        Assert.assertNotNull(test.getVector(new LocalDate(2014,1,1)));
        Assert.assertNull(test.getVector(new LocalDate(2014,1,1)).getStoredObject());
        Assert.assertEquals(test.getVector(new LocalDate(2014,1,1)).getEnd(), new LocalDate(2014,12,31));
        Assert.assertNotNull(test.getVector(new LocalDate(2014,1,1)));
        Assert.assertNull(test.getVector(new LocalDate(2016,12,31)).getStoredObject());
        Assert.assertEquals(test.getVector(new LocalDate(2016,12,31)).getStart(), new LocalDate(2016,8,1));

    }
}
