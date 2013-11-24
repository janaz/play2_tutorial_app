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
import java.util.Arrays;
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

    public List<ColumnStatistics> getStatistics(List<DataCategory> categories) throws IOException {
        List<ColumnStatistics> stats = new ArrayList<>();
        while (stats.size() < categories.size()) {
            stats.add(new ColumnStatistics());
        }
        final CSVReader reader = new CSVReader(getReader(), getSeparator());
        try {
            do {
                String[] next = reader.readNext();
                if (next == null) {
                    break;
                }
                try {
                    List<Comparable<?>> parsedValues = new ArrayList<>(categories.size());

                    for (int idx = 0; idx < stats.size(); idx++) {
                        String stringValue = "";
                        if (idx < next.length) {
                            stringValue = next[idx];
                        }
                        Comparable<?> parsedValue = categories.get(idx).parsedValue(stringValue);
                        parsedValues.add(parsedValue);
                    }
                    for (int idx = 0; idx < stats.size(); idx++) {
                        stats.get(idx).add(parsedValues.get(idx));
                    }
                } catch (Exception e) {

                    //mark next as invalid row
                }
            } while (true);
            return stats;
        } finally {
            reader.close();
        }
    }

    private boolean dataIncludesHeader() {
        List<String> firstLine = Arrays.asList(getDataSample().iterator().next());
        int unknowns = 0;
        for (String s : columnNames(firstLine)) {
            if (s == DataCategory.UNKNOWN.name()) {
                unknowns++;
            }
        }
        if (unknowns * 100 / firstLine.size() > 30) {
            return false;
        } else {
            return true;
        }
    }

    public List<String> headerNames() {
        return columnNames(header());
    }

    private List<String> columnNames(List<String> columns) {
        List<String> out = new ArrayList<>();
        for (String s : columns) {
            out.add(DataCategory.detect(s, null).name());
        }
        return out;
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

    private List<String> header() {
        return Arrays.asList(getDataSample().iterator().next());
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
