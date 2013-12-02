package com.neutrino.csv;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class SampleReader implements LineReadListener {
    private final List<String[]> sampleLines;
    private List<DataCategory> header;
    private long limit;

    public SampleReader(long limit) {
        sampleLines = new ArrayList<>();
        header = null;
        this.limit = limit;
    }

    public List<String[]> getSampleLines() {
        return sampleLines;
    }

    public List<DataCategory> getHeader() {
        return header;
    }

    public List<String> getStringHeader() {
        return Lists.transform(header, new Function<DataCategory, String>() {

            @Nullable
            @Override
            public String apply(@Nullable DataCategory dataCategory) {
                return dataCategory.name();
            }
        });
    }

    @Override
    public Object lineRead(long lineNumber, String[] line, String raw, List<DataCategory> categories) {
        if (line != null) {
            sampleLines.add(line);
        }

        if (header == null) {
            header = categories;
        }
        return null;
    }

    @Override
    public boolean finished() {
        System.out.println("Sample" + limit + " " +sampleLines.size());

        return sampleLines.size() >= limit;
    }


}
