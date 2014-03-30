package com.neutrino.csv;

import java.util.List;

public interface LineReadListener {
    public Object lineRead(CSVLine line, List<CSVDataHeader> headers, boolean last);
    public boolean finished();
}
