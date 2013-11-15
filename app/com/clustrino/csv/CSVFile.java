package com.clustrino.csv;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import models.clustrino.CsvFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSVFile {
    List<String[]> data;

    private CsvFile model;

    public CSVFile(CsvFile model) {
        this.model = model;
    }

    private Reader getReader() throws IOException {
        UploadedFilePersistService persistService = UploadedFilePersistService.getService();
        return persistService.getReader(model);
    }

    private char getSeparator() throws IOException {
        BufferedReader reader = new BufferedReader(getReader());
        try {
            String firstLine = reader.readLine();
            int maxFieldsNumber = 0;
            char separator = ',';
            for (char c : new char[]{',', ';', '|', '\t'}) {
                CSVParser parser = new CSVParser(c);
                int fieldsNumber = parser.parseLine(firstLine).length;
                if (fieldsNumber > maxFieldsNumber) {
                    maxFieldsNumber = fieldsNumber;
                    separator = c;
                }
            }
            return separator;

        } finally {
            reader.close();
        }
    }

    public List<Float> getPopulationInfo() throws IOException {
        List<Float> retVal = new ArrayList<>();
        List<Integer> populated = new ArrayList<>();
        final CSVReader reader = new CSVReader(getReader(), getSeparator());
        try {
            int lines = 0;
            do {
                String[] next = reader.readNext();
                if (next == null) {
                    break;
                }
                lines++;
//                int len = populated.size();
                while (populated.size() < next.length) {
                    populated.add(0);
                }
//                if (populated.size() != len) {
//                    System.out.println("Line " + lines + " Changed size from " + len + " to " + populated.size() + " xxx " + Arrays.toString(next));
//                }
                for (int idx = 0; idx < next.length; idx++) {
                    String s = next[idx];
                    if (s != null && !s.trim().isEmpty()) {
                        populated.set(idx, populated.get(idx).intValue() + 1);
                    }
                }
            } while (true);
            if (lines != 0) {
                for (int i : populated) {
                    retVal.add((float) i / lines);
                }
            }
            return retVal;
        } finally {
            reader.close();
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
        return columnNames(header());
    }

    private String[] columnNames(String[] columns) {
        List<String> out = new ArrayList<>();
        for (String s : columns) {
            out.add(DataCategory.detect(s, null).name());
        }
        return out.toArray(new String[]{});
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
        return getDataSample().iterator().next();
    }

    public List<String[]> dataSample() {
        if (dataIncludesHeader()) {
            return getDataSample().subList(1, getDataSample().size() - 1);
        } else {
            return getDataSample();
        }
    }

    private List<String[]> _getDataSample(int howMany) throws IOException {
        final CSVReader reader = new CSVReader(getReader(), getSeparator());
        try {
            List<String[]> out = new ArrayList<>();
            do {
                String[] next = reader.readNext();
                if (next == null || out.size() >= howMany) {
                    break;
                }
                out.add(next);

            } while (true);
            return out;
        } finally {
            reader.close();
        }
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
