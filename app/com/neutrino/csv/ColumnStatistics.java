package com.neutrino.csv;

import java.util.HashSet;
import java.util.Set;

public class ColumnStatistics {
    private final CSVDataHeader header;
    private int populated;
    private int count;
    private int minLength;
    private int maxLength;
    private int nullCount;
    private Comparable<?> minValue;
    private Comparable<?> maxValue;
    Set<Object> distinct;

    public ColumnStatistics(CSVDataHeader header) {
        this.minLength = -1;
        this.maxLength = -1;
        distinct = new HashSet<>();
        this.header = header;
    }

    public CSVDataHeader getHeader() {
        return header;
    }

    public void add(Comparable<?> parsedValue) {
        count++;

        if (parsedValue == null || parsedValue.toString().isEmpty()) {
            nullCount++;
        } else {
            String stringValue = parsedValue.toString();
            populated++;
            if (minLength == -1 || minLength > stringValue.length()) {
                minLength = stringValue.length();
            }
            if (maxLength == -1 || maxLength < stringValue.length()) {
                maxLength = stringValue.length();
            }
            if (minValue == null || ((Comparable<Object>)parsedValue).compareTo(minValue) < 0) {
                minValue = parsedValue;
            }
            if (maxValue == null || ((Comparable<Object>)parsedValue).compareTo(maxValue) > 0) {
                maxValue = parsedValue;
            }
            //distinct.add(parsedValue);
        }
    }

    public float getPopulatedPercentage() {
        if (count == 0) {
            return 0f;
        }
        return (float) populated / count;
    }

    public int getPopulated() {
        return populated;
    }

    public int getCount() {
        return count;
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getDistinctCount() {
        return distinct.size();
    }

    public int getNullCount() {
        return nullCount;
    }

    public Comparable<?> getMinValue() {
        return minValue;
    }

    public Comparable<?> getMaxValue() {
        return maxValue;
    }

}
