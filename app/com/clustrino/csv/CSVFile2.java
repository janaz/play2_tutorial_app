package com.clustrino.csv;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import models.clustrino.CsvFile;
import org.supercsv.exception.SuperCsvException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSVFile2 {
    List<List<String>> data;

    private CsvFile model;

    public CSVFile2(CsvFile model) {
        this.model = model;
    }

    private Reader getReader() throws IOException {
        UploadedFilePersistService persistService = UploadedFilePersistService.getService();
        return persistService.getReader(model);
    }

    private CsvPreference getCsvPreference() throws IOException {
        CsvPreference preference = CsvPreference.STANDARD_PREFERENCE;
        int maxFieldsNumber = 0;
        char quote ='"';
        for (CsvPreference pref : new CsvPreference[]{
                CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE,
                CsvPreference.EXCEL_PREFERENCE,
                CsvPreference.TAB_PREFERENCE,
                new CsvPreference.Builder(quote, ',', "\n").build(),
                new CsvPreference.Builder(quote, ';', "\n").build(),
                new CsvPreference.Builder(quote, '|', "\n").build(),
                new CsvPreference.Builder(quote, '\t', "\n").build(),
                new CsvPreference.Builder(quote, ',', "\r\n").build(),
                new CsvPreference.Builder(quote, ';', "\r\n").build(),
                new CsvPreference.Builder(quote, '|', "\r\n").build(),
                new CsvPreference.Builder(quote, '\t', "\r\n").build(),
        }) {
            final ICsvListReader csvReader = new CsvListReader(getReader(), pref);
            try {
                int fieldsNumber = csvReader.read().size();
                if (fieldsNumber > maxFieldsNumber) {
                    maxFieldsNumber = fieldsNumber;
                    preference = pref;
                }
            } finally {
                csvReader.close();
            }
        }
        return preference;
    }

    public List<Float> getPopulationInfo() throws IOException {
        final List<Float> retVal = new ArrayList<>();
        final List<Integer> populated = new ArrayList<>();
        final ICsvListReader reader = new CsvListReader(getReader(), getCsvPreference());
        try {
            int lines = 0;
            do {
                List<String> next = null;
                try {
                    next = reader.read();
                }catch (SuperCsvException e) {
                }

                if (next == null) {
                    break;
                }
                lines++;
                int len = populated.size();
                while (populated.size() < next.size()) {
                    populated.add(0);
                }
                if (populated.size() != len) {
                    System.out.println("Line " + lines + " Changed size from " + len + " to " + populated.size());
                }
                for (int idx = 0; idx < next.size(); idx++) {
                    String s = next.get(idx);
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
        List<String> firstLine = getDataSample().iterator().next();
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
        List<Integer> lengths = Lists.transform(getDataSample(), new Function<List<String>, Integer>() {
            @Override
            public Integer apply(List<String> strings) {
                return strings.size();
            }
        });
        return Collections.max(lengths);
    }

    private List<String> header() {
        return getDataSample().iterator().next();
    }

    public List<List<String>> dataSample() {
        if (dataIncludesHeader()) {
            return getDataSample().subList(1, getDataSample().size() - 1);
        } else {
            return getDataSample();
        }
    }

    private List<List<String>> _getDataSample(int howMany) throws IOException {
        final ICsvListReader reader = new CsvListReader(getReader(), getCsvPreference());
        try {
            List<List<String>> out = new ArrayList<>();
            do {
                List<String> next = null;
                try {
                    next = reader.read();
                }catch (SuperCsvException e) {
                }
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

    private List<List<String>> getDataSample() {
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
