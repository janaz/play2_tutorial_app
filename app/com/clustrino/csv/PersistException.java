package com.clustrino.csv;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: tomasz.janowski
 * Date: 10/11/13
 * Time: 9:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class PersistException extends Exception {
    public PersistException(Throwable e) {
        super(e);
    }
}
