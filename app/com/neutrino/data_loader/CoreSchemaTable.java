package com.neutrino.data_loader;

import org.polyjdbc.core.schema.model.AttributeBuilder;
import org.polyjdbc.core.schema.model.RelationBuilder;
import org.polyjdbc.core.schema.model.Schema;

import javax.sql.DataSource;
import java.util.*;


public class CoreSchemaTable {
    private final String name;

    private final CoreSchemaTypeTable typeTable;
    private final Map<String, CoreSchemaColumn> columns = new HashMap<>();
    private final String dbName;

    public CoreSchemaTable(String name) {
        this(name, null);
    }

    public CoreSchemaTable(String name, CoreSchemaTypeTable typeTable) {
        this(name, typeTable, null);
    }

    public CoreSchemaTypeTable getTypeTable() {
        return typeTable;
    }

    protected CoreSchemaTable(String name, CoreSchemaTypeTable typeTable, String dbName) {
        this.name = name;
        this.typeTable = typeTable;
        this.dbName = dbName;
        if (typeTable != null) {
            addColumn(new CoreSchemaColumn(typeTable.getIdColumnName(), "INTEGER").notNull().foreignKey(typeTable));
        }
    }

    public CoreSchemaTable forDb(String dbName) {
        CoreSchemaTypeTable newType = null;
        if (typeTable != null) {
            newType = typeTable.forDb(dbName);
        }
        CoreSchemaTable newTable = new CoreSchemaTable(getName(), newType, dbName);
        for (CoreSchemaColumn column : columns.values()) {
            newTable.addColumn(column.dup());
        }
        return newTable;
    }

    public void selectColumn(String columnName) {
        columns.get(columnName).select();
    }

    public void adjustColumnLength(String columnName, int length) {
        columns.get(columnName).adjustLength(length);
    }

    public CoreSchemaTable addColumn(CoreSchemaColumn c) {
        columns.put(c.getName(), c);
        return this;
    }

    public Collection<CoreSchemaColumn> getColumns() {
        return columns.values();
    }
    public String getName() {
        return name;
    }

    public List<String> updateSchema(Schema schema) {
        if (dbName == null) {
            throw new IllegalStateException("DBName is null");
        }
        List<String> retVal = new ArrayList<>();
        if (typeTable != null) {
            retVal.addAll(typeTable.updateSchema(schema));
        }
        String tabName = fullTableName();
        RelationBuilder builder = schema.addRelation(tabName);
        for (String colName : columns.keySet()) {
            CoreSchemaColumn column = columns.get(colName);
            if (column.isSelected() || allColumnSelected()) {
                AttributeBuilder attributeBuilder = column.buildAttribute(builder);
                if (attributeBuilder != null) {
                    System.out.println("attr builder " + tabName + "." + colName + " not null");
                    builder = attributeBuilder.and();
                    if (column.isPrimary()) {
                        builder = builder.primaryKey(getName() + "_pk").using(colName).and();
                    }
                    if (column.getForeignKey() != null) {
                        System.out.println("attr builder " + tabName + "." + colName + " IS null");

                        String fkName = getName() + "_" + colName + "_fk";
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
        }
        //special handling of PersonHeader table
        if (getName().equals(RefData.PERSON_HEADER_TABLE_NAME)) {
            String q = "ALTER TABLE " + tabName + " ADD UNIQUE INDEX dataSetSourceIdIdx(DataSetID, SourceID)";
            retVal.add(q);
        }
        builder.build();
        return retVal;
    }

    public boolean allColumnSelected() {
        return getName().equals(RefData.PERSON_NAME.getName()) ||
                getName().equals(RefData.PERSON_ADDRESS.getName());
    }

    public String fullTableName() {
        if (dbName == null) {
            throw new IllegalStateException("DBName is null");
        }
        return dbName + "." + getName();
    }

    public void populateTypes(DataSource ds) {
        if (typeTable != null) {
            typeTable.populate(ds);
        }
    }

    public void selectAllColumns() {
        for (String colName : columns.keySet()) {
            selectColumn(colName);
        }
    }
}
