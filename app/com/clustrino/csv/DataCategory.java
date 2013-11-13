package com.clustrino.csv;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: tomasz.janowski
 * Date: 2/11/13
 * Time: 11:37 PM
 * To change this template use File | Settings | File Templates.
 */
public enum DataCategory {
    SOURCE(Pattern.compile("source", Pattern.CASE_INSENSITIVE), null),
    FULL_NAME(Pattern.compile("^(full)?name$", Pattern.CASE_INSENSITIVE), null),
    FIRST_NAME(Pattern.compile("(^(first|given).*name)|(^f[_\\s]*name)|(^(first|given)[_\\s]*n)", Pattern.CASE_INSENSITIVE), null),
    LAST_NAME(Pattern.compile("(^(last|sur|family).*name)|(^l[_\\s]*name)|(^(last|family)[_\\s]*n)", Pattern.CASE_INSENSITIVE), null),
    MIDDLE_NAME(Pattern.compile("(^mid.*name)|(^m[_\\s]*name)|(^mid(dle)?[_\\s]*n)", Pattern.CASE_INSENSITIVE), null),
    AKA_NAME(Pattern.compile("(^aka.*name)|(^aka[_\\s]*n)", Pattern.CASE_INSENSITIVE), null),
    GENDER(Pattern.compile("^(gender|sex)$", Pattern.CASE_INSENSITIVE), null),
    TITLE(Pattern.compile("(^title$)|(salutation)", Pattern.CASE_INSENSITIVE), null),
    PHONE(Pattern.compile("^phone.*?(no|number|num)?$", Pattern.CASE_INSENSITIVE), null),
    HOME_PHONE(Pattern.compile("^(home|landline).*?(phone|no|\\s*number|\\s+num)$", Pattern.CASE_INSENSITIVE), null),
    WORK_PHONE(Pattern.compile("^work.*?(phone|no|number|num)$", Pattern.CASE_INSENSITIVE), null),
    MOBILE_PHONE(Pattern.compile("^mobile.*?(phone|no|number|num)?$", Pattern.CASE_INSENSITIVE), null),
    EMAIL(Pattern.compile("^e.?mail", Pattern.CASE_INSENSITIVE), null),
    WWW(Pattern.compile("(^www)|(^web)", Pattern.CASE_INSENSITIVE), null),
    TWITTER(Pattern.compile("^twitter", Pattern.CASE_INSENSITIVE), null),
    SKYPE(Pattern.compile("^skype", Pattern.CASE_INSENSITIVE), null),
    DATE_OF_BIRTH(Pattern.compile("(^dob)|(^(date|day).*?birth)|(birth.*?(day|date))", Pattern.CASE_INSENSITIVE), null),
    DATE_OF_DEATH(Pattern.compile("(^deceased)|(^(date|day).*?death)|(death.*?(day|date))", Pattern.CASE_INSENSITIVE), null),
    ADDRESS(Pattern.compile("(^home.*?address)|(^address$)", Pattern.CASE_INSENSITIVE), null),
    ADDRESS_TYPE(Pattern.compile("address.*type", Pattern.CASE_INSENSITIVE), null),
    ADDRESS_LINE_1(Pattern.compile("address.*1", Pattern.CASE_INSENSITIVE), null),
    ADDRESS_LINE_2(Pattern.compile("address.*2", Pattern.CASE_INSENSITIVE), null),
    ADDRESS_LINE_3(Pattern.compile("address.*3", Pattern.CASE_INSENSITIVE), null),
    ADDRESS_LINE_4(Pattern.compile("address.*4", Pattern.CASE_INSENSITIVE), null),
    ADDRESS_LINE_5(Pattern.compile("address.*5", Pattern.CASE_INSENSITIVE), null),
    ADDRESS_LINE_6(Pattern.compile("address.*6", Pattern.CASE_INSENSITIVE), null),
    SUBURB(Pattern.compile("suburb", Pattern.CASE_INSENSITIVE), null),
    DISTRICT(Pattern.compile("district", Pattern.CASE_INSENSITIVE), null),
    PROVINCE(Pattern.compile("province", Pattern.CASE_INSENSITIVE), null),
    COUNTY(Pattern.compile("county", Pattern.CASE_INSENSITIVE), null),
    CITY(Pattern.compile("city", Pattern.CASE_INSENSITIVE), null),
    STATE(Pattern.compile("state", Pattern.CASE_INSENSITIVE), null),
    COUNTRY(Pattern.compile("country", Pattern.CASE_INSENSITIVE), null),
    POSTCODE(Pattern.compile("(^zip)|(^p.*code)", Pattern.CASE_INSENSITIVE), null),
    STREET_NAME(Pattern.compile("(^street$)|(^str.*name)", Pattern.CASE_INSENSITIVE), null),
    STREET_TYPE(Pattern.compile("(^str.*type)", Pattern.CASE_INSENSITIVE), null),
    BUILDING_NUMBER(Pattern.compile("^(build|hous|premis).*(no|num)", Pattern.CASE_INSENSITIVE), null),
    APPARTMENT_NUMBER(Pattern.compile("^(appart|apt|door).*(no|num)", Pattern.CASE_INSENSITIVE), null),
    DRIVERS_LICENSE_NUMBER(Pattern.compile("^(driv.*lic.*)", Pattern.CASE_INSENSITIVE), null),
    PASSPORT_NUMBER(Pattern.compile("passport", Pattern.CASE_INSENSITIVE), null),
    ARNO(Pattern.compile("^arno$", Pattern.CASE_INSENSITIVE), null),
    UNKNOWN(null,null);

    private final Pattern namePattern;
    private final Pattern dataPattern;

    public static List<String> names() {
       Collection<String> n = Collections2.transform(Arrays.asList(values()), new Function<DataCategory, String>() {
           @Nullable
           @Override
           public String apply(@Nullable DataCategory dataCategory) {
               return dataCategory.name();
           }
       });
       List<String> listNames = Lists.newArrayList(n);
       Collections.sort(listNames);
       return listNames;
    }

    DataCategory(Pattern namePattern, Pattern dataPatten) {
        this.namePattern = namePattern;
        this.dataPattern = dataPatten;
    }

    private boolean isHeaderMatching(String header) {
        if (namePattern == null) {
            return true;
        }
        return namePattern.matcher(header).find();
    }

    private boolean isDataMatching(Collection<String> dataSample) {
        if (dataSample == null || dataPattern == null) {
            return true;
        }
        int matching = 0;
        for (String s : dataSample) {
                if (dataPattern.matcher(s).find()){
                    matching++;
                }
        }
        return (matching * 100 / dataSample.size()) > 80;
    }

    public static DataCategory detect(String columnName, Collection<String> dataSample) {
        for (DataCategory category : DataCategory.values()) {
            if (category.isHeaderMatching(columnName) && category.isDataMatching(dataSample)) {
                return category;
            }
        }
        return null;
    }

}
