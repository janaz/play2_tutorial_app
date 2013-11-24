package com.clustrino.csv.parsers;

import org.pojava.datetime.DateTime;
import org.pojava.datetime.DateTimeConfig;

public class DateTimeParser implements DataCategoryParser {
    @Override
    public DateTime parse(String value) {
        DateTimeConfig.globalEuropeanDateFormat();
        return DateTime.parse(value.trim());
    }

    @Override
    public String dbType() {
        return "DateTime";
    }

    @Override
    public String dbValue(Object o) {
        DateTime dt = (DateTime)o;
        return dt.toString("%Y-%m-%d %H:%M:%S");
    }
}
