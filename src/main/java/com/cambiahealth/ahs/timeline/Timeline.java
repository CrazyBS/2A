package com.cambiahealth.ahs.timeline;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.*;

/**
 * Created by bpyl on 2/18/2016.
 */
public class Timeline {

    private LinkedList<TimeVector> timeline = new LinkedList<TimeVector>();

    public boolean isEmpty() {
        return timeline.isEmpty();
    }

    public int storeVector(LocalDate start, LocalDate end, Object data) {
        LocalDate startTime = new LocalDate(start);
        LocalDate endTime = new LocalDate(end);

        // Look at existing vectors
        // If this time overlaps an existing,
        // adjust existing vectors, both before and after
        // then insert

        TimeVector newVector = new TimeVector(startTime, endTime, data);

        if(timeline.isEmpty() && null != data) {
            timeline.add(newVector);
        } else if (!timeline.isEmpty()){
            ListIterator<TimeVector> iter = timeline.listIterator();
            boolean isInserted = false;
            while(iter.hasNext()) {
                TimeVector vector = iter.next();

                // Decide if we are inserting in this row or not
                // At this point, we know fell through this level.  So we are likely at
                // our insertion point.
                // null != data  (false)
                // (past the time or no more lines) and not inserted (true)
                if(null != data && !isInserted) {
                    if(vector.getStart().isAfter(startTime)) {
                        iter.previous();
                        iter.add(newVector);
                        iter.next();
                        isInserted = true;
                    } else if (!iter.hasNext()) {
                        iter.add(newVector);
                        isInserted = true;
                    }
                }

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

    public void addAll(Timeline timeline) {
        for(TimeVector vector : timeline.timeline) {
            this.storeVector(vector.getStart(), vector.getEnd(), vector.getStoredObject());
        }
    }

    public List<TimeVector> getTimelineVectors() {
        return timeline;
    }
}
