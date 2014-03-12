package com.neutrino.models.core_common;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.neutrino.models.metadata.ColumnMapping;
import com.neutrino.profiling.CoreSchema;
import play.db.ebean.Model;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class Option {
    private final String tableName;
    private final String columnName;
    private final int userId;
    private String serverName;
    private final Class<? extends CoreType> coreTypeClz;
    private List<String> typesList;

    public Option(String tableName, String columnName, Class<? extends CoreType> coreTypeClz) {
        this(tableName, columnName, coreTypeClz, -1);
    }

    private Option(String tableName, String columnName, Class<? extends CoreType> coreTypeClz, int userId) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.coreTypeClz = coreTypeClz;
        this.userId = userId;
    }

    public String key(String type) {
        return tableName +"|" + columnName + "|" + type;
    }

    private String serverName() {
        if (serverName == null) {
            serverName = new CoreSchema(userId).server().getName();
        }
        return serverName;
    }

    public Option forUser(int userId) {
        return new Option(tableName, columnName, coreTypeClz, userId);
    }

    public String getColumnName() {
        return columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean matches(ColumnMapping mapping, String type) {
        if (!this.tableName.equalsIgnoreCase(mapping.coreTableName)) {
            return false;
        }
        if (!this.columnName.equalsIgnoreCase(mapping.coreAttributeName)) {
            return false;
        }
        if (type == null) {
            return getTypes().isEmpty();
        }

        if (getTypes().contains(type)) {
            return true;
        }

        if (type.equals("CHOOSE") && !getTypes().isEmpty()) {
            return true;
        }
        return false;
    }

    public List<String> getTypes() {
        if (serverName() == null) {
            throw new IllegalStateException("Server name is empty. Use forServerName factory method");
        }
        if (typesList != null) {
            return typesList;
        }
        typesList = Collections.emptyList();
        if (coreTypeClz != CoreType.class) {
            Model.Finder<Integer, CoreType> finder = null;
            try {
                Method m = coreTypeClz.getMethod("find", String.class);
                finder = (Model.Finder) m.invoke(null, new Object[]{serverName()});
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            List<CoreType> coreTypes = finder.all();

            typesList = Lists.transform(coreTypes, new Function<CoreType, String>() {
                @Nullable
                @Override
                public String apply(@Nullable CoreType coreType) {
                    return coreType.getType();
                }
            });
        }
        return typesList;
    }
}
