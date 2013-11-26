package com.clustrino.csv.parsers;

public class StringParser implements DataCategoryParser {
    @Override
    public String parse(String value) {
        return value.trim();
    }

    @Override
    public String dbType() {
        return "Varchar(255)";
    }

    @Override
    public String dbValue(Object o) {
        if (o == null) {
            return null;
        }
        return o.toString();
    }

}
