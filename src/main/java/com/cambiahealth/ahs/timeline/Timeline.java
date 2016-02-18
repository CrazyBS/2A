package com.cambiahealth.ahs.timeline;

import org.joda.time.Days;
import org.joda.time.LocalTime;

import java.util.*;

/**
 * Created by bpyl on 2/18/2016.
 */
public class Timeline {

    private class TimeVector {
        private LocalTime start;
        private LocalTime end;

        public TimeVector(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        public LocalTime getStart() {
            return start;
        }

        public void setStart(LocalTime start) {
            this.start = start;
        }

        public LocalTime getEnd() {
            return end;
        }

        public void setEnd(LocalTime end) {
            this.end = end;
        }
    }

    private LinkedList<TimeVector> timeline = new LinkedList<TimeVector>();

    public boolean isEmpty() {
        return timeline.isEmpty();
    }

    public int storeVector(Date start, Date end, Object data) {
        LocalTime startTime = new LocalTime(start);
        LocalTime endTime = new LocalTime(end);

        // Look at existing vectors
        // If this time overlaps an existing,
        // adjust existing vectors, both before and after
        // then insert

        TimeVector newVector = new TimeVector(startTime, endTime);

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
                    iter.add(new TimeVector(endTime.plus(Days.days(1)), vector.getEnd()));
                    vector.setEnd(startTime.minus(Days.days(1)));
                    continue;
                }

                // If vector start before, but ends within, trim end
                if(vector.getStart().isBefore(startTime)
                        && vector.getEnd().isBefore(endTime) && vector.getEnd().isAfter(startTime)) {
                    vector.setEnd(startTime.minus(Days.days(1)));
                    continue;
                }

                // Delete any vector that is contained within our new one
                if(vector.getStart().isAfter(startTime) && vector.getEnd().isBefore(endTime)) {
                    iter.remove();
                    continue;
                }

                // If vector starts within, but ends after, trim start
                if(vector.getStart().isAfter(startTime) && vector.getStart().isBefore(endTime)
                        && vector.getEnd().isAfter(endTime)) {
                    vector.setStart(endTime.plus(Days.days(1)));
                }
            }

        }


        return Days.daysBetween(startTime, endTime).getDays();
    }

    public Object get(Date index) {
        return null;
    }
}
