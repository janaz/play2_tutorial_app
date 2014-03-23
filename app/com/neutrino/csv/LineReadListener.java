package com.neutrino.csv;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tomasz.janowski
 * Date: 21/11/13
 * Time: 11:01 PM
 * To change this template use File | Settings | File Templates.
 */
public interface LineReadListener {
    public Object lineRead(long lineNumber, String[] line, String raw, List<CSVDataHeader> headers, boolean last);
    public boolean finished();
}
