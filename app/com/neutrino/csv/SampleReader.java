package com.neutrino.csv;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class SampleReader implements LineReadListener {
    private final List<String[]> sampleLines;
    private List<CSVDataHeader> dataHeaders;
    private final long limit;

    public SampleReader(long limit) {
        sampleLines = new ArrayList<>();
        dataHeaders = null;
        this.limit = limit;
    }

    public List<String[]> getSampleLines() {
        return sampleLines;
    }

    public List<CSVDataHeader> getHeaders() {
        return dataHeaders;
    }

    public List<String> getStringHeaders() {
        return Lists.newArrayList(Lists.transform(dataHeaders, new Function<CSVDataHeader, String>() {

            @Nullable
            @Override
            public String apply(@Nullable CSVDataHeader header) {
                return header.name();
            }
        }));
    }

    @Override
    public Object lineRead(long lineNumber, String[] line, String raw, List<CSVDataHeader> headers, boolean last) {
        if (line != null) {
            sampleLines.add(line);
        }

        if (dataHeaders == null) {
            dataHeaders = headers;
        }
        return null;
    }

    @Override
    public boolean finished() {
        return sampleLines.size() >= limit;
    }


}
