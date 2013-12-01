package com.clustrino.csv.parsers;

public class GenderParser implements DataCategoryParser {
    @Override
    public String parse(String value) {
        String cleanVal = value.trim().toUpperCase();
        if (cleanVal.startsWith("F")) {
            return "F";
        } else if (cleanVal.startsWith("M")) {
            return "M";
        } else if (cleanVal.startsWith("U") || cleanVal.isEmpty()) {
            return "U";
        } else if (cleanVal.isEmpty()){
            return null;
        } else {
            return cleanVal;
        }
    }

    @Override
    public String dbType() {
        return "CHAR(1)";
    }

    @Override
    public String dbValue(Object o) {
        if (o == null) {
            return null;
        }
        return o.toString();
    }

}
