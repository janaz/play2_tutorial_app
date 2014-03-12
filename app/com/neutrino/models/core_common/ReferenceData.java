package com.neutrino.models.core_common;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.neutrino.profiling.CoreSchema;
import com.neutrino.profiling.PrecoreSchema;
import org.reflections.Reflections;
import play.db.ebean.Model;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.*;

//-> and table_name not like '%Type'
//        -> and table_name <> 'PersonHeader'
//        -> and column_name not like '%ID'
//        -> and column_name not like '%Timestamp'
//        -> and column_name not like 'Full%'import java.lang.reflect.Field;
public class ReferenceData {

    private final String serverName;
    private final static List<Option> globalOptions;
    private final int userId;
    private List<Option> options;


    private ReferenceData(int userId, String serverName) {
        this.userId = userId;
        this.serverName = serverName;//(;
    }

    public static ReferenceData forCore(int userId) {
        return new ReferenceData(userId, (new CoreSchema(userId)).server().getName());
    }

    public static ReferenceData forPrecore(int userId) {
        return new ReferenceData(userId, (new PrecoreSchema(userId)).server().getName());
    }

    static {
        globalOptions = new ArrayList<>();

        final Reflections reflections = new Reflections("com.neutrino.models");

        for (Class clz : reflections.getSubTypesOf(Model.class)) {
            Table t = (Table) clz.getAnnotation(Table.class);
            for (Field f : clz.getFields()) {
                SelectableAttribute attr = f.getAnnotation(SelectableAttribute.class);
                if (attr != null) {
                    Column c = f.getAnnotation(Column.class);
                    addOption(t.name(), c.name(), attr.type());
                }
            }
        }
    }

    public Option findByKey(String key) {
        String[] elements = key.split("|");
        String type = null;
        if (elements.length > 2) {
            type = elements[3];
        }
        for (Option o : globalOptions) {
            if (o.key(type).equals(key)) {
                return o.forUser(userId);
            }
        }
        return null;
    }

    public List<Option> getOptions() {
        if (options == null) {
            options = Lists.transform(globalOptions, new Function<Option, Option>() {
                @Nullable
                @Override
                public Option apply(@Nullable Option option) {
                    return option.forUser(userId);
                }
            });
        }
        return options;
    }

    public Map<String, Map<String, List<String>>> toJSON() {
        Map<String, Map<String, List<String>>> map = new HashMap<>();
        for (Option o : getOptions()) {
            Map<String, List<String>> el = map.get(o.getTableName());
            if (el == null) {
                el = new HashMap<>();
                map.put(o.getTableName(), el);
            }
            el.put(o.getColumnName(), o.getTypes());
        }
        return map;
    }

    private static void addOption(String tableName, String columnName, Class<? extends CoreType> coreTypeClz) {
        globalOptions.add(new Option(tableName, columnName, coreTypeClz));
    }


    private void createPersonNameType(Integer id, String type) {
        PersonNameType t = new PersonNameType();
        t.id = id;
        t.type = type;
        t.save(serverName);
    }

    private void createPersonExternalIdentifierType(Integer id, String type) {
        PersonExternalIdentifierType t = new PersonExternalIdentifierType();
        t.id = id;
        t.type = type;
        t.save(serverName);
    }

    private void createPersonAnniversaryType(Integer id, String type) {
        PersonAnniversaryType t = new PersonAnniversaryType();
        t.id = id;
        t.type = type;
        t.save(serverName);
    }

    private void createPersonPhoneType(Integer id, String type) {
        PersonPhoneType t = new PersonPhoneType();
        t.id = id;
        t.type = type;
        t.save(serverName);
    }

    private void createPersonAddressType(Integer id, String type) {
        PersonAddressType t = new PersonAddressType();
        t.id = id;
        t.type = type;
        t.save(serverName);
    }

    public void createReferenceData() {
        createPersonNameType(1, "Legal Name");
        createPersonNameType(2, "Preferred Name");
        createPersonNameType(3, "Business Name");
        createPersonNameType(4, "Alias Name");
        createPersonNameType(5, "Maiden Name");

        createPersonExternalIdentifierType(1, "Passport");
        createPersonExternalIdentifierType(2, "Driver License");
        createPersonExternalIdentifierType(3, "Photo ID");

        createPersonAnniversaryType(1, "Date of Birth");
        createPersonAnniversaryType(2, "Date of Death");
        createPersonAnniversaryType(3, "Date of Join");

        createPersonPhoneType(1, "Home phone");
        createPersonPhoneType(2, "Mobile phone");
        createPersonPhoneType(3, "Business phone");
        createPersonPhoneType(4, "Fax");

        createPersonAddressType(1, "Residential");
        createPersonAddressType(2, "Postal");
        createPersonAddressType(3, "Business");
        createPersonAddressType(4, "Delivery");
        createPersonAddressType(5, "Summer Residence");
        createPersonAddressType(6, "PO Box");
    }
}
