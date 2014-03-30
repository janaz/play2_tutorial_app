package com.neutrino.data_loader;

import org.polyjdbc.core.schema.model.AttributeBuilder;
import org.polyjdbc.core.schema.model.RelationBuilder;
import org.polyjdbc.core.schema.model.Schema;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CoreSchemaTable {
    private final String name;
    private final CoreSchemaTypeTable typeTable;
    private final Map<String, CoreSchemaColumn> columns = new HashMap<>();
    private final int userId;
    public CoreSchemaTable(String name) {
        this(name, null);
    }

    public CoreSchemaTable(String name, CoreSchemaTypeTable typeTable) {
        this(name, typeTable, -1);
    }

    private CoreSchemaTable(String name, CoreSchemaTypeTable typeTable, int userId) {
        this.name = name;
        this.typeTable = typeTable;
        this.userId = userId;
        if (typeTable != null) {
            addColumn(new CoreSchemaColumn(typeTable.getIdColumnName(), "INTEGER").notNull().foreignKey(typeTable));
        }
    }

    public CoreSchemaTable forUser(int userId) {
        CoreSchemaTypeTable newType = null;
        if (typeTable != null) {
            newType = typeTable.forUser(userId);
        }
        CoreSchemaTable newTable = new CoreSchemaTable(getName(), newType, userId);
        for (CoreSchemaColumn column: columns.values()) {
            newTable.addColumn(column.dup());
        }
        return newTable;
    }

    public CoreSchemaTable addColumn(CoreSchemaColumn c) {
        columns.put(c.getName(), c);
        return this;
    }

    public String getName() {
        return name;
    }

    public List<String> updateSchema(Schema schema) {
        if (userId <= 0) {
            throw new IllegalStateException("User ID should be > 0");
        }
        List<String> retVal = new ArrayList<>();
        if (typeTable != null) {
            retVal.addAll(typeTable.updateSchema(schema));
        }
        String tabName = fullTableName();
        RelationBuilder builder = schema.addRelation(tabName);
        for (String colName : columns.keySet()) {
            CoreSchemaColumn column = columns.get(colName);
            AttributeBuilder attributeBuilder = column.buildAttribute(builder);
            if (attributeBuilder != null) {
                System.out.println("attr builder "+tabName+"."+colName+" not null");
                builder = attributeBuilder.and();
                if (column.isPrimary()){
                    builder = builder.primaryKey(getName()+"_pk").using(colName).and();
                }
                if (column.getForeignKey() != null) {
                    System.out.println("attr builder " + tabName + "." + colName + " IS null");

                    String fkName =getName()+"_"+colName+"_fk";
                    builder = builder.foreignKey(fkName).on(colName).references(column.getForeignKey().getName(), colName).and();
                }
            } else {
                String q = "ALTER TABLE " + tabName + " ADD COLUMN `" + colName + "` " + column.getType();
                if (column.isNotNull()) {
                    q = q + " NOT NULL";
                }
                retVal.add(q);
            }
        }
        builder.build();
        return retVal;
    }

    public String fullTableName() {
        if (userId <= 0) {
            throw new IllegalStateException("User ID should be > 0");
        }
        return String.format("Core%03d.%s", this.userId, getName());
    }

    public void populateTypes(DataSource ds) {
        if (typeTable != null) {
            typeTable.populate(ds);
        }
    }
}
