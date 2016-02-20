package com.cambiahealth.ahs.timeline;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.*;

/**
 * Created by bpyl on 2/18/2016.
 */
public class Timeline {

    private List<TimeVector> timeline = new ArrayList<TimeVector>();

    public boolean isEmpty() {
        return timeline.isEmpty();
    }

    public int storeVector(LocalDate start, LocalDate end, Map<String,String> data) {
        LocalDate startTime = new LocalDate(start);
        LocalDate endTime = new LocalDate(end);

        // Look at existing vectors
        // If this time overlaps an existing,
        // adjust existing vectors, both before and after
        // then insert

        TimeVector newVector = new TimeVector(startTime, endTime, data);

        timeline.add(newVector);

        return Math.abs(Days.daysBetween(startTime, endTime).getDays());
    }

    public Map<String, String> get(LocalDate index) {
        // Find the date that intersects this one and return the object
        ListIterator<TimeVector> iter = timeline.listIterator(timeline.size());
        LocalDate day = new LocalDate(index);

        while(iter.hasPrevious()) {
            TimeVector vector = iter.previous();
            if((vector.getStart().isBefore(day) || vector.getStart().isEqual(day))
                    && vector.getEnd().isAfter(day) || vector.getEnd().isEqual(day)) {
                return vector.getStoredObject();
            }
        }

        return null;
    }

    public void addAll(Timeline timeline) {
        for(TimeVector vector : timeline.timeline) {
            this.storeVector(vector.getStart(), vector.getEnd(), vector.getStoredObject());
        }
    }

    public void removeAll(Timeline timeline) {
        for(TimeVector vector : timeline.timeline) {
            this.storeVector(vector.getStart(), vector.getEnd(), null);
        }
    }
}
