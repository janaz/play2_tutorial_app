package com.neutrino.csv;

/**
 * Created with IntelliJ IDEA.
 * User: tomasz.janowski
 * Date: 24/11/13
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class CSVError {
    private final long lineNumber;
    private final String text;

    public CSVError(CSVLine line) {
        this.lineNumber = line.getLineNumber();
        if (line.getRaw().length() > 4096) {
            this.text = line.getRaw().substring(0, 4096);
        } else {
            this.text = line.getRaw();
        }
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public String getText() {
        return text;
    }
}
