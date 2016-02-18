package com.cambiahealth.ahs.timeline;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.*;

/**
 * Created by bpyl on 2/18/2016.
 */
public class Timeline {

    private class TimeVector {
        private LocalDate start;
        private LocalDate end;
        private Object storedObject;

        public TimeVector(LocalDate start, LocalDate end, Object storedObject) {
            this.start = start;
            this.end = end;
            this.storedObject = storedObject;
        }

        public LocalDate getStart() {
            return start;
        }

        public void setStart(LocalDate start) {
            this.start = start;
        }

        public LocalDate getEnd() {
            return end;
        }

        public void setEnd(LocalDate end) {
            this.end = end;
        }

        public Object getStoredObject() {
            return storedObject;
        }

        public void setStoredObject(Object storedObject) {
            this.storedObject = storedObject;
        }
    }

    private LinkedList<TimeVector> timeline = new LinkedList<TimeVector>();

    public boolean isEmpty() {
        return timeline.isEmpty();
    }

    public int storeVector(Date start, Date end, Object data) {
        LocalDate startTime = new LocalDate(start);
        LocalDate endTime = new LocalDate(end);

        // Look at existing vectors
        // If this time overlaps an existing,
        // adjust existing vectors, both before and after
        // then insert

        TimeVector newVector = new TimeVector(startTime, endTime, data);

        if(timeline.isEmpty()) {
            timeline.add(newVector);
        } else {
            ListIterator<TimeVector> iter = timeline.listIterator();
            while(iter.hasNext()) {
                TimeVector vector = iter.next();

                // Skip any vector that is not overlapping our new one
                if(vector.getEnd().isBefore(startTime)) {
                    continue;
                }

                // Slice (insert) a vector that contains our new one
                if(vector.getStart().isBefore(startTime) && vector.getEnd().isAfter(endTime)) {
                    iter.add(new TimeVector(endTime.plus(Days.days(1)), vector.getEnd(), vector.getStoredObject()));
                    vector.setEnd(startTime.minus(Days.days(1)));
                    continue;
                }

                // If vector start before, but ends within, trim end
                if(vector.getStart().isBefore(startTime)
                        && (vector.getEnd().isBefore(endTime) || vector.getEnd().isEqual(endTime))
                        && (vector.getEnd().isAfter(startTime) || vector.getEnd().isEqual(startTime))) {
                    vector.setEnd(startTime.minus(Days.days(1)));
                    continue;
                }

                // At this point, we know fell through this this level.  So we are likely at
                // our insertion point.
                iter.add(newVector);

                // Delete any vector that is contained within our new one
                if((vector.getStart().isAfter(startTime) || vector.getStart().isEqual(startTime))
                        && (vector.getEnd().isBefore(endTime) || vector.getEnd().isEqual(endTime))) {
                    iter.remove();
                    continue;
                }

                // If vector starts within, but ends after, trim start
                if((vector.getStart().isAfter(startTime) || vector.getStart().isEqual(startTime))
                        && (vector.getStart().isBefore(endTime) || vector.getStart().isEqual(endTime))
                        && vector.getEnd().isAfter(endTime)) {
                    vector.setStart(endTime.plus(Days.days(1)));
                    continue;
                }

                // Stop once we are past the end date
                if(vector.getStart().isAfter(endTime)) {
                    break;
                }
            }
        }

        return Math.abs(Days.daysBetween(startTime, endTime).getDays());
    }

    public Object get(LocalDate index) {
        // Find the date that intersects this one and return the object
        ListIterator<TimeVector> iter = timeline.listIterator();
        LocalDate day = new LocalDate(index);

        while(iter.hasNext()) {
            TimeVector vector = iter.next();
            if((vector.getStart().isBefore(day) || vector.getStart().isEqual(day))
                    && vector.getEnd().isAfter(day) || vector.getEnd().isEqual(day)) {
                return vector.getStoredObject();
            }
        }

        return null;
    }

    public boolean checkConsistency() {
        return true;
    }
}
