package com.neutrino.csv;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gdata.util.io.base.UnicodeReader;
import com.neutrino.models.metadata.DataSet;
import org.mozilla.universalchardet.UniversalDetector;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        try (BufferedReader reader = new BufferedReader(getReader())) {
            this.firstLine = reader.readLine();
            return this.firstLine;
        }
    }

    private String encoding() throws IOException {
        if (!doEncodingPerformed) {
            encodingValue = doEncoding();
            doEncodingPerformed = true;
        }
        return encodingValue;
    }

    private String doEncoding() throws IOException {
        byte[] buf = new byte[1024 * 32];
        int limit = 1024 * 1024 * 64; //64MB should be enough
        InputStream fis = getInputStream();
        // (1)
        UniversalDetector detector = new UniversalDetector(null);
        int totalRead = 0;
        String encoding;
        try {
            // (2)
            int nread;
            while ((totalRead < limit) && (nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
                totalRead += nread;
            }
            // (3)
            detector.dataEnd();
        } finally {
            encoding = detector.getDetectedCharset();
            detector.reset();
            fis.close();
        }
        // (4)
        System.out.print("Encoding detector read " + totalRead + " bytes");
        if (encoding != null) {
            System.out.println("Detected encoding = " + encoding);
        } else {
            System.out.println("No encoding detected.");
        }

        return encoding;
    }

    public char getSeparator() throws IOException {
        int maxFieldsNumber = 0;
        char separator = ',';
        for (char c : new char[]{',', ';', '|', '\t'}) {
            StolenCSVParser parser = new StolenCSVParser(c);
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
        Set<String> uniq = new HashSet<>();
        for (CSVDataHeader h : internalHeaders()) {
            if (h.isEmpty()) {
                System.out.println("Found empty value - not a header");
                memoizeDataIcludesHeader = false;
                return memoizeDataIcludesHeader;
            }
            if (h.isNumber()) {
                System.out.println("Found a number " + h.name() + " - not a header");

                memoizeDataIcludesHeader = false;
                return memoizeDataIcludesHeader;
            }
            if (uniq.contains(h.name())) {
                System.out.println("Found non unique name " + h.name() + " - not a header");

                memoizeDataIcludesHeader = false; // non unique
                return memoizeDataIcludesHeader;
            } else {
                uniq.add(h.name());
            }
        }
        System.out.println("There is a header");

        memoizeDataIcludesHeader = true;
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
            StolenCSVParser parser = new StolenCSVParser(getSeparator());
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

    private void lineRead(CSVLine line, boolean last) throws IOException {
        for (LineReadListener l : lineReadListeners) {
            l.lineRead(line, headers(), last);
        }
    }

    private boolean finished() {
        for (LineReadListener l : lineReadListeners) {
            if (!l.finished()) {
                return false;
            }
        }
        return true;
    }

    public void readFile() throws IOException {
        long lineNumber = 0;

        try (final BufferedReader reader = new BufferedReader(getReader(), 1024 * 32)) {
            if (dataIncludesHeader()) {
                reader.readLine();
                lineNumber++;
            }
            final StolenCSVParser parser = new StolenCSVParser(getSeparator());
            do {
                final String line = reader.readLine();
                if (line == null) {
                    lineRead(new CSVLine(lineNumber, null, null), true);
                    break;
                }
                lineNumber++;
                try {
                    String[] next = parser.parseLine(line);
                    lineRead(new CSVLine(lineNumber, next, line), false);
                } catch (IOException e) {
                    lineRead(new CSVLine(lineNumber, null, line), false);
                    System.out.println(e);
                }
            } while (!finished());
        }
    }

}
