package com.neutrino.models.core;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.neutrino.models.metadata.ColumnMapping;
import play.db.ebean.Model;
import scala.annotation.meta.getter;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class Option {
    private final String tableName;
    private final String columnName;
    private final String serverName;
    private final Class<? extends CoreType> coreTypeClz;
    private List<String> typesList;

    public Option(String tableName, String columnName, Class<? extends CoreType> coreTypeClz) {
        this(tableName, columnName, coreTypeClz, null);
    }

    private Option(String tableName, String columnName, Class<? extends CoreType> coreTypeClz, String serverName) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.coreTypeClz = coreTypeClz;
        this.serverName = serverName;
    }

    public Option forServerName(String serverNameParam) {
        return new Option(tableName, columnName, coreTypeClz, serverNameParam);
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
        if (serverName == null) {
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
                finder = (Model.Finder) m.invoke(null, new Object[]{serverName});
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
