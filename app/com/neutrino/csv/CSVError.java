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

    public CSVError(long lineNumber, String text) {
        this.lineNumber = lineNumber;
        if (text.length() > 4096) {
            this.text = text.substring(0, 4096);
        } else {
            this.text = text;
        }
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public String getText() {
        return text;
    }
}
