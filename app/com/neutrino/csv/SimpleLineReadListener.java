package com.neutrino.csv;

import java.util.List;


public class SimpleLineReadListener implements LineReadListener {
    @Override
    public Object lineRead(long lineNumber, String[] line, String raw, List<CSVDataHeader> headers) {
        return null;
    }

    @Override
    public boolean finished() {
        return false;
    }

}
