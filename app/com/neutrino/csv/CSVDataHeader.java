package com.neutrino.csv;

import com.neutrino.csv.parsers.DataCategoryParser;
import com.neutrino.csv.parsers.StringParser;

public class CSVDataHeader {

    private final DataCategoryParser parser;
    private final String columnName;
    private final DataColumnCategory category;

    public String name() {
        return columnName;
    }

    public String value() {
        return columnName;
    }

    public String dataType() {
        return "STRING";
    }

    public CSVDataHeader(String columnName) {
        this.parser = StringParser.instance();
        this.columnName = cleanupString(columnName);
        this.category = DataColumnCategory.detect(columnName, null);
    }

    @Override
    public int hashCode() {
        return columnName.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CSVDataHeader)) {
            return false;
        }
        return columnName == ((CSVDataHeader) other).columnName;
    }

    private static String cleanupString(String s) {
        return s.trim().toUpperCase().replaceAll("[^\\w\\d\\.-]", "_").replaceAll("_+", "_");
    }

    public boolean isEmpty() {
        return columnName == null || columnName.isEmpty() || columnName.equals("_");
    }

    public boolean isNumber() {
        if (isEmpty()) {
            return false;
        }
        try {
            long l = Long.valueOf(columnName);
            return true;
        } catch (NumberFormatException npe) {
            return false;
        }
    }

    public Comparable<?> parsedValue(String value) {
        return parser.parse(value);
    }

    public String dbValue(Object parsedValue) {
        return parser.dbValue(parsedValue);
    }

    public String dbParsedValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    public String dbType() {
        return parser.dbType();
    }

    public DataColumnCategory category() {
        return this.category;

    }
}
