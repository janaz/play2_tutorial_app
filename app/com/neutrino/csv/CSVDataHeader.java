package com.neutrino.csv;

import com.neutrino.csv.parsers.DataCategoryParser;
import com.neutrino.csv.parsers.StringParser;

public class CSVDataHeader {

    private final DataCategoryParser parser;
    private final String columnName;
    private final String originalColumnName;
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
        this.originalColumnName = columnName;
        this.parser = StringParser.instance();
        this.columnName = cleanupString(columnName);
        this.category = DataColumnCategory.detect(this.originalColumnName, null);
    }

    private static String cleanupString(String s) {
        return s.trim().toUpperCase().replaceAll("[^\\w\\d]", "_").replaceAll("_+", "_");
    }

    public Comparable<?> parsedValue(String value) {
        return parser.parse(value);
    }

    public String dbValue(Object parsedValue) {
        return parser.dbValue(parsedValue);
    }

    public String dbType() {
        return parser.dbType();
    }
    
    public DataColumnCategory category() {
        return this.category;
        
    }
}
