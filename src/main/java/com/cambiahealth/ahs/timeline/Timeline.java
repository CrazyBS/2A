package com.cambiahealth.ahs.timeline;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.*;

/**
 * Created by bpyl on 2/18/2016.
 */
public class Timeline {

    private List<TimeVector> timeline = new ArrayList<TimeVector>();
    private Map<String, String> consistentData = new HashMap<String, String>();

    public boolean isEmpty() {
        return timeline.isEmpty();
    }

    public int storeVector(LocalDate start, LocalDate end, Map<String,String> data) {
        return storeVector(start, end, data, null);
    }

    public int storeVector(LocalDate start, LocalDate end, Map<String,String> data, Timeline dependency) {
        TimeVector vector = new TimeVector(start, end, data);
        if(this.equals(timeline)) {
            throw new IllegalArgumentException("A timeline cannot have a dependency of itself!");
        }
        if(null != dependency) {
            vector.setDependency(dependency.timeline);
        }
        timeline.add(vector);
        return Math.abs(Days.daysBetween(start, end).getDays());
    }

    public void addConsistentData(String key, String data) {
        consistentData.put(key, data);
    }

    public Map<String, String> get(LocalDate index) {
        return get(index, this.timeline);
    }

    private Map<String, String> get(LocalDate index, List<TimeVector> timeline) {
        TimeVector vector = getVector(index, timeline);
        return null == vector ? null : vector.getStoredObject();
    }

    public TimeVector getVector(LocalDate index) {
        return getVector(index, this.timeline);
    }

    private TimeVector getVector(LocalDate index, List<TimeVector> timeline) {
        // Find the date that intersects this one and return the object
        ListIterator<TimeVector> iter = timeline.listIterator(timeline.size());
        LocalDate day = new LocalDate(index);

        while(iter.hasPrevious()) {
            TimeVector vector = iter.previous();
            if((vector.getStart().isBefore(day) || vector.getStart().isEqual(day))
                    && vector.getEnd().isAfter(day) || vector.getEnd().isEqual(day)) {

                TimeVector newVector = new TimeVector(vector.getStart(), vector.getEnd(), getStoredData(vector.getStoredObject()));
                newVector.setDependency(vector.getDependency());

                // We have a vector that matches
                // Do we have a dependant vector?
                if (null == vector.getDependency()) {
                    return newVector;
                } else {
                    return (null == getVector(index, vector.getDependency())) ? null : newVector;
                }

            }
        }

        return null;
    }

    public void addAll(Timeline timeline) {
        addAll(timeline, null);
    }

    public void addAll(Timeline timeline, Timeline dependency) {
        for(TimeVector vector : timeline.timeline) {
            this.storeVector(vector.getStart(), vector.getEnd(), vector.getStoredObject(), dependency);
        }
        // Overwrite consistent data
        this.consistentData.putAll(timeline.consistentData);
    }

    public void removeAll(Timeline timeline) {
        for(TimeVector vector : timeline.timeline) {
            this.storeVector(vector.getStart(), vector.getEnd(), null);
        }
        // Ignore consistent data?
    }

    private Map<String, String> getStoredData(Map<String, String> data) {
        if(null != data) {
            Map<String, String> newData = new HashMap<String, String>(data);
            newData.putAll(consistentData);
            return newData;
        } else {
            return null;
        }
    }
}
