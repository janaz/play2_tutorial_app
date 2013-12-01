package com.clustrino.csv.parsers;

public class StringParser implements DataCategoryParser {
    @Override
    public String parse(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
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
