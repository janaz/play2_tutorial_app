package com.clustrino.csv;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import models.clustrino.CsvFile;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CSVFile {
    private CsvFile model;
    private String firstLine;
    List<LineReadListener> lineReadListeners;
    List<DataCategory> categories;

    public CSVFile(CsvFile model) {
        this.model = model;
        lineReadListeners = new ArrayList<>();
    }

    private Reader getReader() throws IOException {
        UploadedFilePersistService persistService = UploadedFilePersistService.getService();
        return persistService.getReader(model);
    }

    private String getFirstLine() throws IOException {
        if (this.firstLine != null) {
            return this.firstLine;
        }
        BufferedReader reader = new BufferedReader(getReader());
        try {
            this.firstLine = reader.readLine();
            return this.firstLine;
        } finally {
            reader.close();
        }
    }

    private List<DataCategory> categoriesInFirstLine() throws IOException {
        List<DataCategory> retVal = Lists.transform(originalHeaders(), new Function<String, DataCategory>() {
            @Nullable
            @Override
            public DataCategory apply(@Nullable String s) {
                return DataCategory.detect(s, null);
            }
        });
        return retVal;
    }

    private char getSeparator() throws IOException {
        int maxFieldsNumber = 0;
        char separator = ',';
        for (char c : new char[]{',', ';', '|', '\t'}) {
            CSVParser parser = new CSVParser(c);
            int fieldsNumber = parser.parseLine(this.getFirstLine()).length;
            if (fieldsNumber > maxFieldsNumber) {
                maxFieldsNumber = fieldsNumber;
                separator = c;
            }
        }
        return separator;
    }

    private boolean dataIncludesHeader() throws IOException {
        int unknowns = 0;
        for (DataCategory cat : categoriesInFirstLine()) {
            if (cat == DataCategory.UNKNOWN) {
                unknowns++;
            }
        }
        if (unknowns * 100 / categoriesInFirstLine().size() > 30) {
            return false;
        } else {
            return true;
        }
    }

    private List<String> originalHeaders() throws IOException {
        CSVParser parser = new CSVParser(getSeparator());
        return Lists.newArrayList(parser.parseLine(this.getFirstLine()));
    }


    public List<DataCategory> categories() throws IOException {
        if (this.categories == null) {
            this.categories = model.getHeaderCategories();
            if (this.categories == null) {
                this.categories = categoriesInFirstLine();
            }
        }
        return this.categories;
    }

    public void addReadListener(LineReadListener l) {
        this.lineReadListeners.add(l);
    }

    private void lineRead(long lineNumber, String[] line, String raw) throws IOException {
        for (LineReadListener l : lineReadListeners) {
            l.lineRead(lineNumber, line, raw, categories());
        }
    }

    private boolean finished() {
        boolean fin = true;
        for (LineReadListener l : lineReadListeners) {
            if (fin) fin = l.finished();
        }
        return fin;
    }

    public void readFile_old() throws IOException {
        long lineNumber = 0;
        final CSVReader reader = new CSVReader(getReader(), getSeparator());
        try {
            if (dataIncludesHeader()) {
                reader.readNext();
                lineNumber++;
            }
            do {
                final String[] next = reader.readNext();
                lineNumber++;
                if (next == null) {
                    break;
                }
                lineRead(lineNumber, next, null);
            } while (!finished());
        } finally {
            reader.close();
        }
    }

    public void readFile() throws IOException {
        long lineNumber = 0;
        final BufferedReader reader = new BufferedReader(getReader());
        try {
            if (dataIncludesHeader()) {
                reader.readLine();
                lineNumber++;
            }
            final CSVParser parser = new CSVParser(getSeparator());
            do {

                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                String[] next = null;
                try {
                    next = parser.parseLine(line);
                } catch (Exception e) {
                    System.out.println(e);
                    System.out.println(e.getStackTrace());
                }
                lineNumber++;
                lineRead(lineNumber, next, line);
            } while (!finished());
        } finally {
            reader.close();
        }
    }

}
