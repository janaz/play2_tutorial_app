package com.neutrino.csv.parsers;

import org.pojava.datetime.DateTime;
import org.pojava.datetime.DateTimeConfig;

public class DateTimeParser implements DataCategoryParser {
    private static final DateTime REF_DT = DateTime.parse("1000-10-10");
    @Override
    public DateTime parse(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        DateTimeConfig.globalEuropeanDateFormat();
        DateTime dt = DateTime.parse(value.trim());
        if (dt.compareTo(REF_DT) > 0) {
            return dt;
        } else {
            return null;
        }
    }

    @Override
    public String dbType() {
        return "DateTime";
    }

    @Override
    public String dbValue(Object o) {
        if (o == null) {
            return null;
        }
        DateTime dt = (DateTime)o;
        return dt.toString("yyyy-MM-dd HH:mm:ss");
    }
}
