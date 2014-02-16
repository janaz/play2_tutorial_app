package com.neutrino.csv;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import com.neutrino.models.metadata.DataColumn;
import com.neutrino.models.metadata.DataSet;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CSVFile {
    private DataSet model;
    private String firstLine;
    List<LineReadListener> lineReadListeners;
    private List<CSVDataHeader> memoizedHeaders;
    private List<CSVDataHeader> memoizedUnknownHeaders;

    public CSVFile(DataSet model) {
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

    public char getSeparator() throws IOException {
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

    public boolean dataIncludesHeader() throws IOException {
        int unknowns = 0;
        int all = 0;
        for (CSVDataHeader h : internalHeaders()) {
            if (h.category() == DataColumnCategory.UNKNOWN) {
                unknowns++;
            }
            all++;
        }
        if (unknowns * 100 / all > 30) {
            return false;
        } else {
            return true;
        }
    }

    public List<CSVDataHeader> headers() throws IOException {
        if (dataIncludesHeader()) {
            return internalHeaders();
        } else {
            return internalUnknownHeaders();
        }
    }

    private List<CSVDataHeader> internalUnknownHeaders() throws IOException {
        if (this.memoizedUnknownHeaders == null) {
            this.memoizedUnknownHeaders = new ArrayList<>(internalHeaders().size());
            for (int i = 1; i <= internalHeaders().size(); i++) {
                this.memoizedUnknownHeaders.add(new CSVDataHeader("UNKNOWN_" + i));
            }
        }
        return this.memoizedUnknownHeaders;
    }

    private List<CSVDataHeader> internalHeaders() throws IOException {
        if (this.memoizedHeaders == null) {
            CSVParser parser = new CSVParser(getSeparator());
            List<String> list = Lists.newArrayList(parser.parseLine(this.getFirstLine()));
            this.memoizedHeaders = Lists.transform(list, new Function<String, CSVDataHeader>() {
                @Nullable
                @Override
                public CSVDataHeader apply(@Nullable String s) {
                    return new CSVDataHeader(s);
                }
            });
        }
        return this.memoizedHeaders;
    }

    public void addReadListener(LineReadListener l) {
        this.lineReadListeners.add(l);
    }

    private void lineRead(long lineNumber, String[] line, String raw) throws IOException {
        for (LineReadListener l : lineReadListeners) {
            l.lineRead(lineNumber, line, raw, headers());
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
