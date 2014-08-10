package com.neutrino.models.core_common;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.neutrino.data_loader.CoreSchemaColumn;
import com.neutrino.data_loader.CoreSchemaTable;
import com.neutrino.data_loader.RefData;
import org.reflections.Reflections;
import play.db.ebean.Model;

import javax.annotation.Nullable;
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
        return new ReferenceData(userId, null);
    }

    public static ReferenceData forPrecore(int userId) {
        return new ReferenceData(userId, null);
    }

    static {
        globalOptions = new ArrayList<>();

        for (CoreSchemaTable tab : RefData.PRECORE_SCHEMA.getTables()) {
            for (CoreSchemaColumn col : tab.getColumns()) {
                if (col.isFull()) {
                    addOption(tab.getName(), col.getName());
                }
            }
            for (CoreSchemaColumn col : tab.getColumns()) {
                if (col.isSelectable()) {
                    addOption(tab.getName(), col.getName());
                }
            }
        }
    }

    private static Class<Model> getModelClassByName(String className, String pkg) {
        final Reflections reflections = new Reflections(pkg);
        for (Class clz : reflections.getSubTypesOf(Model.class)) {
            if (clz.getSimpleName().equals(className)) {
                return clz;
            }
        }
        return null;
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

    public boolean isUniqueMapping(String tableName, String columnName) {
        for (CoreSchemaTable tab : RefData.PRECORE_SCHEMA.getTables()) {
            if (tab.getName().equals(tableName)) {
                for (CoreSchemaColumn col : tab.getColumns()) {
                    if (col.getName().equals(columnName)) {
                        return !col.isFull();
                    }
                }
            }
        }
        return true;
    }

    public List<Option> getOptions() {
        if (options == null) {
            options = Lists.newArrayList(Lists.transform(globalOptions, new Function<Option, Option>() {
                @Nullable
                @Override
                public Option apply(@Nullable Option option) {
                    return option.forUser(userId);
                }
            }));
        }
        return options;
    }

    public Map<String, Map<String, Collection<String>>> toJSON() {
        Map<String, Map<String, Collection<String>>> map = new HashMap<>();
        for (Option o : getOptions()) {
            Map<String, Collection<String>> el = map.get(o.getTableName());
            if (el == null) {
                el = new HashMap<>();
                map.put(o.getTableName(), el);
            }
            el.put(o.getColumnName(), o.getTypes());
        }
        return map;
    }

    private static void addOption(String tableName, String columnName) {
        globalOptions.add(new Option(tableName, columnName));
    }


}
