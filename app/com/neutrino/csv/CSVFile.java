package com.neutrino.csv;

import au.com.bytecode.opencsv.CSVParser;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gdata.util.io.base.UnicodeReader;
import com.neutrino.models.metadata.DataSet;
import org.mozilla.universalchardet.UniversalDetector;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVFile {
    private final DataSet model;
    private String firstLine;
    private final List<LineReadListener> lineReadListeners;
    private List<CSVDataHeader> memoizedHeaders;
    private List<CSVDataHeader> memoizedUnknownHeaders;
    private Boolean memoizeDataIcludesHeader;
    private String encodingValue;
    private boolean doEncodingPerformed;

    public CSVFile(DataSet model) {
        this.model = model;
        lineReadListeners = new ArrayList<>();
        doEncodingPerformed = false;
    }

    private InputStream getInputStream() throws IOException {
        UploadedFilePersistService persistService = UploadedFilePersistService.getService();
        return persistService.getInputStream(model);
    }

    private Reader getReader() throws IOException {
        return new UnicodeReader(getInputStream(), encoding());
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
    private String encoding() throws IOException {
        if (!doEncodingPerformed){
            encodingValue = doEncoding();
            doEncodingPerformed = true;
        }
        return encodingValue;
    }

    private String doEncoding() throws IOException {
        byte[] buf = new byte[4096];
        int limit = 1024*1024*64; //64MB should be enough
        InputStream fis = getInputStream();
        // (1)
        UniversalDetector detector = new UniversalDetector(null);
        int totalread = 0;
        try {
            // (2)
            int nread;
            while ((totalread < limit) && (nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
                totalread +=nread;
            }
            // (3)
            detector.dataEnd();
        } finally {
            fis.close();
        }
        // (4)
        System.out.print("Encoding detector read " + totalread + " bytes");
        String encoding = detector.getDetectedCharset();
        if (encoding != null) {
            System.out.println("Detected encoding = " + encoding);
        } else {
            System.out.println("No encoding detected.");
        }

        // (5)
        detector.reset();
        return encoding;
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
        if (memoizeDataIcludesHeader != null) {
            return memoizeDataIcludesHeader;
        }
        int unknowns = 0;
        int all = 0;
        for (CSVDataHeader h : internalHeaders()) {
            if (h.category() == DataColumnCategory.UNKNOWN) {
                unknowns++;
            }
            System.out.println("Got category " + h.category() + " for column " + h.name());
            all++;
        }
        System.out.println("Percentage unknown: " + (unknowns * 100 / all));
        if (unknowns * 100 / all > 40) {
            memoizeDataIcludesHeader = false;
        } else {
            memoizeDataIcludesHeader = true;
        }
        return memoizeDataIcludesHeader;
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

            List<CSVDataHeader> transformed = Lists.transform(list, new Function<String, CSVDataHeader>() {
                @Nullable
                @Override
                public CSVDataHeader apply(@Nullable String s) {
                    return new CSVDataHeader(s);
                }
            });
            this.memoizedHeaders = Lists.newArrayList(transformed);
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
