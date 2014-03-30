package com.neutrino.csv;

public class CSVLine {
    private final String[] values;
    private final String raw;
    private final long lineNumber;

    public CSVLine(long lineNumber, String[] values, String raw) {
        this.values = values;
        this.raw = raw;
        this.lineNumber = lineNumber;
    }

    public String[] getValues() {
        return values;
    }

    public String getRaw() {
        return raw;
    }

    public long getLineNumber() {
        return lineNumber;
    }
}
