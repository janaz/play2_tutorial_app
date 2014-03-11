package com.neutrino.models.core;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.neutrino.profiling.CoreSchema;
import org.reflections.Reflections;
import play.db.ebean.Model;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

//-> and table_name not like '%Type'
//        -> and table_name <> 'PersonHeader'
//        -> and column_name not like '%ID'
//        -> and column_name not like '%Timestamp'
//        -> and column_name not like 'Full%'import java.lang.reflect.Field;
public class ReferenceData {

    private final String serverName;
    private final static List<Option> globalOptions;
    private List<Option> options;

    public ReferenceData(int userId) {
        this.serverName = (new CoreSchema(userId)).server().getName();
    }

    static {
        globalOptions = new ArrayList<>();
        Package pkg = ReferenceData.class.getPackage();

        final Reflections reflections = new Reflections(pkg.getName());

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

    public List<Option> getOptions() {
        if (options == null) {
            options = Lists.transform(globalOptions, new Function<Option, Option>() {
                @Nullable
                @Override
                public Option apply(@Nullable Option option) {
                    return option.forServerName(serverName);
                }
            });
        }
        return options;
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
