package com.cambiahealth.ahs.timeline;

import org.joda.time.LocalDate;

/**
 * Created by bpyl on 2/19/2016.
 */
public class TimeVector {
    private LocalDate start;
    private LocalDate end;
    private Object storedObject;

    public TimeVector(LocalDate start, LocalDate end, Object storedObject) {
        this.start = start;
        this.end = end;
        this.storedObject = storedObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeVector that = (TimeVector) o;

        if (!start.equals(that.start)) return false;
        if (!end.equals(that.end)) return false;
        return storedObject != null ? storedObject.equals(that.storedObject) : that.storedObject == null;

    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + (storedObject != null ? storedObject.hashCode() : 0);
        return result;
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
