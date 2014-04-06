package com.neutrino.models.core_common;

import com.neutrino.data_loader.CoreSchemaTable;
import com.neutrino.data_loader.RefData;
import com.neutrino.models.metadata.ColumnMapping;

import java.util.Collection;
import java.util.Collections;

public class Option {
    private final String tableName;
    private final String columnName;
    private final int userId;
    private Collection<String> typesList;

    public Option(String tableName, String columnName) {
        this(tableName, columnName, -1);
    }

    private Option(String tableName, String columnName, int userId) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.userId = userId;
    }

    public String key(String type) {
        return tableName + "|" + columnName + "|" + type;
    }


    public Option forUser(int userId) {
        return new Option(tableName, columnName, userId);
    }

    public String getColumnName() {
        return columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean matches(ColumnMapping mapping) {
        if (!this.tableName.equalsIgnoreCase(mapping.coreTableName)) {
            return false;
        }
        if (!this.columnName.equalsIgnoreCase(mapping.coreAttributeName)) {
            return false;
        }
        return true;
    }

    public Collection<String> getTypes() {
        CoreSchemaTable tab = RefData.PRECORE_SCHEMA.getTable(tableName);
        typesList = Collections.emptyList();
        if (tab.getTypeTable() != null) {
            typesList = tab.getTypeTable().getValues();
        }
        return typesList;
    }
}
