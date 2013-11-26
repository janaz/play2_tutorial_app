package com.clustrino.csv.parsers;

import java.math.BigDecimal;

public class DecimalParser implements DataCategoryParser {
    @Override
    public BigDecimal parse(String value) {
        String cleanVal = value.trim().replace(",", "").replace(" ","");
        if (cleanVal.isEmpty()){
            return null;
        } else {
            return new BigDecimal(value.trim());
        }
    }

    @Override
    public String dbType() {
        return "Decimal(20,10)";
    }

    @Override
    public String dbValue(Object o) {
        if (o == null) {
            return null;
        }
        return o.toString();
    }
}
