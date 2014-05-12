package com.neutrino.data_loader;

/**
 * Created by tomasz.janowski on 13/04/2014.
 */
public class Cleansing {
    public static String FORMAT_VALUE_EXPR_LEVEL_2 = "" +
            "upper(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(lower(trim(%COLUMN_NAME%)),'b', 'a'), 'c', 'a'), 'd', 'a'), 'e', 'a'), 'f', 'a'), 'g', 'a'), 'h', 'a'), 'i', 'a'), 'j', 'a'), 'k', 'a'), 'l', 'a'), 'm', 'a'), 'n', 'a'), 'o', 'a'), 'p', 'a'), 'q', 'a'), 'r', 'a'), 's', 'a'), 't', 'a'), 'u', 'a'), 'v', 'a'), 'w', 'a'), 'x', 'a'), 'y', 'a'), 'z', 'a'), '0', '9'), '1', '9'), '2', '9'), '3', '9'), '4', '9'), '5', '9'), '6', '9'), '7', '9'), '8', '9')) ";

    public static String DATE_VALUE_EXPR_LEVEL_3 = "" +
            "  CASE \n"+
            "    WHEN %FORMAT% = '9999-99-99' and %LAST_TWO_DIGITS% > '12' THEN str_to_date(%COLUMN_NAME%, '%Y-%m-%d') \n"+
            "    WHEN %FORMAT% = '9999-99-99' and %LAST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%Y-%d-%m') \n"+
            "    WHEN %FORMAT% = '99-99-9999' and %FIRST_TWO_DIGITS% > '12' THEN str_to_date(%COLUMN_NAME%, '%d-%m-%Y') \n"+
            "    WHEN %FORMAT% = '99-99-9999' and %FIRST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%m-%d-%Y') \n"+
            "    WHEN %FORMAT% = '99-99-99' and %FIRST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%m-%d-%y') \n"+
            "    WHEN %FORMAT% = '99-99-99' and %FIRST_TWO_DIGITS% between '13' and '31' THEN str_to_date(%COLUMN_NAME%, '%d-%m-%y') \n"+
            "    WHEN %FORMAT% = '99-99-99' and %LAST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%y-%d-%m') \n"+
            "    WHEN %FORMAT% = '99-99-99' and %LAST_TWO_DIGITS% between '13' and '31' THEN str_to_date(%COLUMN_NAME%, '%y-%m-%d') \n"+
            "    WHEN %FORMAT% = '9999/99/99' and %LAST_TWO_DIGITS% > '12' THEN str_to_date(%COLUMN_NAME%, '%Y/%m/%d') \n"+
            "    WHEN %FORMAT% = '9999/99/99' and %LAST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%Y/%d/%m') \n"+
            "    WHEN %FORMAT% = '99/99/9999' and %FIRST_TWO_DIGITS% > '12' THEN str_to_date(%COLUMN_NAME%, '%d/%m/%Y') \n"+
            "    WHEN %FORMAT% = '99/99/9999' and %FIRST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%m/%d/%Y') \n"+
            "    WHEN %FORMAT% = '99/99/99' and %FIRST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%m/%d/%y') \n"+
            "    WHEN %FORMAT% = '99/99/99' and %FIRST_TWO_DIGITS% between '13' and '31' THEN str_to_date(%COLUMN_NAME%, '%d/%m/%y') \n"+
            "    WHEN %FORMAT% = '99/99/99' and %LAST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%y/%d/%m') \n"+
            "    WHEN %FORMAT% = '99/99/99' and %LAST_TWO_DIGITS% between '13' and '31' THEN str_to_date(%COLUMN_NAME%, '%y/%m/%d') \n"+
            "    WHEN %FORMAT% = '9999.99.99' and %LAST_TWO_DIGITS% > '12' THEN str_to_date(%COLUMN_NAME%, '%Y.%m.%d') \n"+
            "    WHEN %FORMAT% = '9999.99.99' and %LAST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%Y.%d.%m') \n"+
            "    WHEN %FORMAT% = '99.99.9999' and %FIRST_TWO_DIGITS% > '12' THEN str_to_date(%COLUMN_NAME%, '%d.%m.%Y') \n"+
            "    WHEN %FORMAT% = '99.99.9999' and %FIRST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%m.%d.%Y') \n"+
            "    WHEN %FORMAT% = '99.99.99' and %FIRST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%m.%d.%y') \n"+
            "    WHEN %FORMAT% = '99.99.99' and %FIRST_TWO_DIGITS% between '13' and '31' THEN str_to_date(%COLUMN_NAME%, '%d.%m.%y') \n"+
            "    WHEN %FORMAT% = '99.99.99' and %LAST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%y.%d.%m') \n"+
            "    WHEN %FORMAT% = '99.99.99' and %LAST_TWO_DIGITS% between '13' and '31' THEN str_to_date(%COLUMN_NAME%, '%y.%m.%d') \n"+
            "    WHEN %FORMAT% = '99999999' and %FIRST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%m%d%Y') \n"+
            "    WHEN %FORMAT% = '99999999' and %FIRST_TWO_DIGITS% between '13' and '31' THEN str_to_date(%COLUMN_NAME%, '%d%m%Y') \n"+
            "    WHEN %FORMAT% = '99999999' and %LAST_TWO_DIGITS% <= '12' THEN str_to_date(%COLUMN_NAME%, '%Y%d%m') \n"+
            "    WHEN %FORMAT% = '99999999' and %LAST_TWO_DIGITS% between '13' and '31' THEN str_to_date(%COLUMN_NAME%, '%Y%m%d') \n"+
            "    WHEN %FORMAT% = '99-AAA-99' and %FIRST_TWO_DIGITS% <= '31' THEN str_to_date(%COLUMN_NAME%, '%d-%b-%y') \n"+
            "    WHEN %FORMAT% = '99-AAA-99' and %FIRST_TWO_DIGITS% > '31' THEN str_to_date(%COLUMN_NAME%, '%y-%b-%d') \n"+
            "    WHEN %FORMAT% = '9999-AAA-99' THEN str_to_date(%COLUMN_NAME%, '%Y-%b-%d') \n"+
            "    WHEN %FORMAT% = '99-AAA-9999' THEN str_to_date(%COLUMN_NAME%, '%d-%b-%Y') \n"+
            "    ELSE null \n"+
            "  END \n";
    
}
