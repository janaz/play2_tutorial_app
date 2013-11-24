package com.clustrino.csv.parsers;

public interface DataCategoryParser {
    public Comparable<?> parse(String value);
    public String dbType();
    public String dbValue(Object o);
}
