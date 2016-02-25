package com.cambiahealth.ahs.entity;

/**
 * Created by msnook on 2/20/2016.
 */
public class Column {
    public String columnValue;
    public int columnLength;

    public Column() {
        this("", 0);
    }

    public Column(String value){
        this(value, 0);
    }

    public Column(String value, int length){
        columnValue = value;
        columnLength = length;
    }

    public String getColumnValue(){
        return columnValue;
    }

    public void setColumnValue(String value){
        columnValue = value;
    }

    public int getColumnLength(){
        return columnLength;
    }

    public void setColumnLength(int length){
        columnLength = length;
    }
}
