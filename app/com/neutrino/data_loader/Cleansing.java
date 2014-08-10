package com.neutrino.data_loader;

/**
 * Created by tomasz.janowski on 13/04/2014.
 */
public class Cleansing {

    public static String COUNTRY_CODE_VAL_4 = "  case \n" +
            "    when %STR_LEN% = 11 and %LPF% = 1 then substr(%COLUMN_NAME%, 1, 2)\n" +
            "    when %STR_LEN% = 13 and %LDZF% = 1 then substr(%COLUMN_NAME%, 3, 2)\n" +
            "    else null\n" +
            "  end\n";

    public static String AREA_CODE_VAL_4 = "case \n" +
            "    when %STR_LEN% = 9 and %LPF% = 0 and %LZF% = 0 then substr(%COLUMN_NAME%, 1, 1)\n" +
            "    when %STR_LEN% = 10 and %LZF% = 1 and %LDZF% = 0 then substr(%COLUMN_NAME%, 2, 1)\n" +
            "    when %STR_LEN% = 11 and %LPF% = 1 then substr(%COLUMN_NAME%, 3, 1)\n" +
            "    when %STR_LEN% = 13 and %LDZF% = 1 then substr(%COLUMN_NAME%, 5, 1)\n" +
            "    else null\n" +
            "  end ";
    public static String PHONE_NUMBER_VAL_4 = "case \n" +
            "    when %STR_LEN% = 8 and %LPF% = 0 and %LZF% = 0 then %COLUMN_NAME%\n" +
            "    when %STR_LEN% = 9 and %LPF% = 0 and %LZF% = 0 then substr(%COLUMN_NAME%, 2)\n" +
            "    when %STR_LEN% = 10 and %LZF% = 1 and %LDZF% = 0 then substr(%COLUMN_NAME%, 3)\n" +
            "    when %STR_LEN% = 11 and %LPF% = 1 then substr(%COLUMN_NAME%, 4)\n" +
            "    when %STR_LEN% = 13 and %LDZF% = 1 then substr(%COLUMN_NAME%, 6)\n" +
            "    else null\n" +
            "  end ";
    
    public static String PHONE_VALUE_EXPR_2 = "" +
            "replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(" +
            "replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(" +
            "replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(" +
            "replace(replace(replace(replace(replace(replace(replace(replace(replace(if(locate('EXT', %COLUMN_NAME%) = 0, " +
            "%COLUMN_NAME%, substring(%COLUMN_NAME%, 1, locate('EXT', %COLUMN_NAME%) - 1)), 'A', ''), 'B', ''), 'C', ''), 'D', ''), " +
            "'E', ''), 'F', ''), 'G', ''), 'H', ''), 'I', ''), 'J', ''), 'K', ''), 'L', ''), 'M', ''), 'N', ''), 'O', ''), " +
            "'P', ''), 'Q', ''), 'R', ''), 'S', ''), 'T', ''), 'U', ''), 'V', ''), 'W', ''), 'X', ''), " +
            "'Y', ''), 'Z', ''), ' ', ''), '-', ''), '(', ''), ')', ''), '.', ''), '#', ''), '&', ''), " +
            "'*', ''), '%', ''), '$', ''), '@', ''), '!', ''), '<', ''), '>', ''), ',', ''), '`', '')";

    public static String PHONE_EXT_VALUE_EXPR_2 = "" +
            "if(locate('EXT', %COLUMN_NAME%) = 0, null, replace(replace(replace(replace(replace(replace(" +
            "replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(" +
            "replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(" +
            "replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(" +
            "replace(replace(replace(replace(substring(%COLUMN_NAME%, locate('EXT', %COLUMN_NAME%) + 3), " +
            "'A', ''), 'B', ''), 'C', ''), 'D', ''), 'E', ''), 'F', ''), 'G', ''), 'H', ''), 'I', ''), " +
            "'J', ''), 'K', ''), 'L', ''), 'M', ''), 'N', ''), 'O', ''), 'P', ''), 'Q', ''), 'R', ''), " +
            "'S', ''), 'T', ''), 'U', ''), 'V', ''), 'W', ''), 'X', ''), 'Y', ''), 'Z', ''), ' ', ''), " +
            "'-', ''), '(', ''), ')', ''), '.', ''), '#', ''), '&', ''), '*', ''), '%', ''), '$', ''), " +
            "'@', ''), '!', ''), '<', ''), '>', ''), ',', ''), '`', ''), '+', ''))";

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
