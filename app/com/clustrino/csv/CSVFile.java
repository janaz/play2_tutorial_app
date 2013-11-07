package com.clustrino.csv;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSVFile {
    List<String[]> data;

    private String filename;

    public CSVFile(String filename) {
        this.filename = filename;
    }

    private Reader getReader() throws IOException {
        return new FileReader(filename);
    }

    private char getSeparator() throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(getReader());
            String firstLine = reader.readLine();
            int maxFieldsNumber = 0;
            char separator = ',';
            for (char c : new char[] {',','|','\t'}) {
                CSVParser parser = new CSVParser(c);
                int fieldsNumber = parser.parseLine(firstLine).length;
                if (fieldsNumber > maxFieldsNumber) {
                    maxFieldsNumber = fieldsNumber;
                    separator = c;
                }
            }
            return separator;

        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }


    private boolean dataIncludesHeader() {
        String[] firstLine = getDataSample().iterator().next();
        int unknowns = 0;
        for (String s : columnNames(firstLine)) {
            if (s == DataCategory.UNKNOWN.name()) {
                unknowns++;
            }
        }
        if (unknowns * 100 / firstLine.length > 30) {
            return false;
        } else {
            return true;
        }
    }

    public String[] headerNames() {
        return columnNames(getDataSample().iterator().next());
    }

    private String[] columnNames(String[] columns) {
        List<String> out = new ArrayList<>();
        for (String s : columns) {
            out.add(DataCategory.detect(s, null).name());
        }
        return out.toArray(new String[]{});
    }

    private String[] madeUpHeader() {
        return new String[]{"a", "b", "C"};
    }

    private int maxCols() {
        List<Integer> lengths = Lists.transform(getDataSample(), new Function<String[], Integer>() {
            @Override
            public Integer apply(String[] strings) {
                return strings.length;
            }
        });
        return Collections.max(lengths);
    }

    private String[] header() {
        if (this.dataIncludesHeader()) {
            return getDataSample().iterator().next();
        } else {
            return this.madeUpHeader();
        }
    }

    public List<String[]> dataSample() {
        if (dataIncludesHeader()) {
            return getDataSample().subList(1, getDataSample().size() - 1);
        } else {
            return getDataSample();
        }
    }

    private List<String[]> _getDataSample(int howMany) throws IOException {

        char separator = getSeparator();
        CSVReader reader = new CSVReader(getReader(), separator);
        List<String[]> out = new ArrayList<>();
        do {
            String[] next = reader.readNext();
            if (next == null || out.size() >= howMany) {
                break;
            }
            out.add(next);

        } while (true);
        return out;
    }

    private List<String[]> getDataSample() {
        if (data == null) {
            try {
                data = _getDataSample(1000);
            } catch (IOException e) {
                data = Collections.EMPTY_LIST;
            }
        }
        return data;
    }

}
