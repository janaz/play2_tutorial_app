package com.clustrino.csv;

public class PersistException extends Exception {
    public PersistException(Throwable e) {
        super(e);
    }

    public PersistException(String s) {
        super(s);
    }
}
