package com.neutrino.csv;

import java.util.List;


public class SimpleLineReadListener implements LineReadListener {
    @Override
    public Object lineRead(CSVLine line, List<CSVDataHeader> headers, boolean last) {
        return null;
    }

    @Override
    public boolean finished() {
        return false;
    }

}
