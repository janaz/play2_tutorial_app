package com.neutrino.data_loader;

import com.google.common.base.Joiner;
import com.neutrino.datamappingdiscovery.CollectionUtils;
import com.neutrino.models.metadata.ColumnMapping;
import com.neutrino.models.metadata.DataSet;
import com.neutrino.profiling.EbeanServerManager;
import com.neutrino.profiling.MetadataSchema;
import com.neutrino.profiling.PrecoreSchema;
import com.neutrino.profiling.StagingSchema;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.util.*;

public class PrecoreDataLoader {
    private final PrecoreSchema precoreSchema;
    private final int userId;
    private final MetadataSchema metadataSchema;

    public PrecoreDataLoader(int userId) {
        this.userId = userId;
        this.precoreSchema = new PrecoreSchema(userId);
        this.metadataSchema = new MetadataSchema(userId);
    }

    private static class TableAttribute {
        private final String tableName;
        private final String attributeName;

        public TableAttribute(String tableName, String attrName) {
            this.tableName = tableName;
            this.attributeName = attrName;
        }

        public String getTableName() {
            return this.tableName;
        }

        public String getAttributeName() {
            return this.attributeName;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof TableAttribute)) {
                return false;
            }
            TableAttribute o = (TableAttribute) other;
            return ((tableName == null && o.tableName == null) || (tableName != null && tableName.equals(o.tableName))) &&
                    ((attributeName == null && o.attributeName == null) || (attributeName != null && attributeName.equals(o.attributeName)))
                    ;
        }

        @Override
        public int hashCode() {
            if (this.attributeName == null || this.tableName == null) {
                return 10;
            }
            return 31 * this.attributeName.hashCode() + this.tableName.hashCode();
        }
    }

    private static class TableType {
        private final String tableName;
        private final String attributeType;

        public TableType(String tableName, String attrType) {
            this.tableName = tableName;
            this.attributeType = attrType;
        }

        public String getTableName() {
            return this.tableName;
        }

        public String getAttributeType() {
            return this.attributeType;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof TableType)) {
                return false;
            }
            TableType o = (TableType) other;
            return ((tableName == null && o.tableName == null) || (tableName != null && tableName.equals(o.tableName))) &&
                    ((attributeType == null && o.attributeType == null) || (attributeType != null && attributeType.equals(o.attributeType)))
                    ;
        }

        @Override
        public int hashCode() {
            if (this.attributeType == null || this.tableName == null) {
                return 10;
            }
            return 31 * this.attributeType.hashCode() + this.tableName.hashCode();
        }
    }

    private List<DataSet> dataSets() {
        MetadataSchema mtd = new MetadataSchema(userId);
        List<DataSet> dataSets = DataSet.find(mtd.server().getName()).where().eq("state", DataSet.State.MANUAL_MAPPING_DONE).findList();
        return dataSets;
    }

    private Map<TableAttribute, Collection<ColumnMapping>> columnMappingsForUser() {
        List<ColumnMapping> mappings = new ArrayList<>();
        for (DataSet ds : dataSets()) {
            mappings.addAll(ds.getMappings());
        }
        Map<TableAttribute, Collection<ColumnMapping>> map = CollectionUtils.listAsMap(mappings, new CollectionUtils.ListToMapConverter<TableAttribute, ColumnMapping>() {
            @Override
            public TableAttribute getKey(ColumnMapping item) {
                return new TableAttribute(item.getCoreTableName(), item.getCoreAttributeName());
            }
        });
        return map;
    }

    private Map<String, Collection<TableAttribute>> columnMappingByTableName() {
        Map<TableAttribute, Collection<ColumnMapping>> map = columnMappingsForUser();

        return CollectionUtils.listAsMap(map.keySet(), new CollectionUtils.ListToMapConverter<String, TableAttribute>() {
            @Override
            public String getKey(TableAttribute item) {
                return item.getTableName();
            }
        });

    }

    private DataSource getDataSource() {
        return EbeanServerManager.dataSource();
    }

    private void createPrecoreSchema() {
        Map<TableAttribute, Collection<ColumnMapping>> map = columnMappingsForUser();
        Map<String, Collection<TableAttribute>> byTableName = columnMappingByTableName();

        Map<String, CoreSchemaTable> tabByName = CollectionUtils.listAsUniqueMap(RefData.PRECORE_TABLES, new CollectionUtils.ListToMapConverter<String, CoreSchemaTable>() {
            @Override
            public String getKey(CoreSchemaTable item) {
                return item.getName();
            }
        });
        Map<String, CoreSchemaTable> coreTabByName = CollectionUtils.listAsUniqueMap(RefData.CORE_TABLES, new CollectionUtils.ListToMapConverter<String, CoreSchemaTable>() {
            @Override
            public String getKey(CoreSchemaTable item) {
                return item.getName();
            }
        });
        CoreSchema mySchema = new CoreSchema("Precore");
        CoreSchema myCoreSchema = new CoreSchema("Core");
        mySchema.addTable(RefData.PERSON_HEADER);
        myCoreSchema.addTable(RefData.PERSON_HEADER);
        for (String tabName : byTableName.keySet()) {
            if (tabName == null) {
                continue;
            }
            CoreSchemaTable table = tabByName.get(tabName);
            CoreSchemaTable coreTable = coreTabByName.get(tabName);
            System.out.println("Table for " + tabName + " : " + table);
            if (!table.getName().equals(RefData.PERSON_HEADER.getName())) {
                mySchema.addTable(table);
                myCoreSchema.addTable(coreTable);
            }
        }
        mySchema = mySchema.forUser(userId);
        myCoreSchema = myCoreSchema.forUser(userId);
        for (String tabName : byTableName.keySet()) {
            if (tabName == null) {
                continue;
            }
            CoreSchemaTable table = mySchema.getTable(tabName);
            CoreSchemaTable coreTable = myCoreSchema.getTable(tabName);
            for (TableAttribute key : byTableName.get(tabName)) {
                String attributeName = key.getAttributeName();
                if (attributeName == null) {
                    table.selectAllColumns();

                } else {
                    table.selectColumn(attributeName);
                    coreTable.selectColumn(attributeName);
                    ColumnMapping maxLenMapping = Collections.max(map.get(key), new Comparator<ColumnMapping>() {
                        @Override
                        public int compare(ColumnMapping o1, ColumnMapping o2) {
                            Integer max1 = o1.getDataColumn().getResultsColumns().get(0).getMaximumLength();
                            Integer max2 = o2.getDataColumn().getResultsColumns().get(0).getMaximumLength();
                            return max1.compareTo(max2);
                        }
                    });
                    table.adjustColumnLength(attributeName, maxLenMapping.getDataColumn().getResultsColumns().get(0).getMaximumLength());
                    coreTable.adjustColumnLength(attributeName, maxLenMapping.getDataColumn().getResultsColumns().get(0).getMaximumLength());
                }
            }
        }
        mySchema.create(getDataSource());
        mySchema.populate(getDataSource());
        myCoreSchema.create(getDataSource());
        myCoreSchema.populate(getDataSource());

    }

    public void populate() {
        PrecoreSchema prec = new PrecoreSchema(userId);
        com.neutrino.profiling.CoreSchema cor = new com.neutrino.profiling.CoreSchema(userId);
        if (!prec.isCreated() && !cor.isCreated()) {
            prec.createDatabase();
            cor.createDatabase();
            createPrecoreSchema();
        }
        for (DataSet ds : dataSets()) {
            List<ColumnMapping> dsMappings = ds.getMappings();
            if (dsMappings.isEmpty()) {
                continue;
            }
            Map<TableType, Collection<ColumnMapping>> byTableType = CollectionUtils.listAsMap(dsMappings, new CollectionUtils.ListToMapConverter<TableType, ColumnMapping>() {
                @Override
                public TableType getKey(ColumnMapping item) {
                    return new TableType(item.getCoreTableName(), item.getCoreAttributeType());
                }
            });
            TableType headerTabType = new TableType(RefData.PERSON_HEADER.getName(), null);

            Collection<ColumnMapping> headerMappings = byTableType.get(headerTabType);
            System.out.println(headerMappings);
            processTableType(ds, headerTabType, headerMappings, headerMappings);
            for (TableType tabType : byTableType.keySet()) {
                if (tabType.getTableName() == null ||
                        tabType.getTableName().equals(RefData.PERSON_HEADER.getName())) {
                    continue;
                }

                Collection<ColumnMapping> tabTypeMappings = byTableType.get(tabType);
                processTableType(ds, tabType, headerMappings, tabTypeMappings);
            }
        }
    }

    private void processTableType(DataSet ds, TableType tabType, Collection<ColumnMapping> headerMappings, Collection<ColumnMapping> tabTypeMappings) {
        Map<String, String> attrNameToStgName = new HashMap<>();
        List<String> attributes = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (ColumnMapping mapping : tabTypeMappings) {
            if (!tabType.getTableName().equals(mapping.getCoreTableName())) {
                throw new RuntimeException("Table Name mismatch1!!! " + tabType.getTableName() + "<->" + mapping.getCoreTableName());
            }
            String attrName = mapping.getCoreAttributeName();
            String stgName = mapping.getDataColumn().getName();
            if (attrName == null) {
                if (mapping.getCoreTableName().equals(RefData.PERSON_NAME.getName())) {
                    attrName = RefData.FULL_NAME_COLUMN;
                } else if (mapping.getCoreTableName().equals(RefData.PERSON_NAME.getName())) {
                    attrName = RefData.FULL_ADDRESS_LINE_COLUMN;
                } else {
                    throw new RuntimeException("Attribute Name null for dataset " + ds.id + " table name" + mapping.getCoreTableName());
                }
            }
            if (attrNameToStgName.get(attrName) != null) {
                throw new RuntimeException("Column name not unique! "+attrName);
            }
            attrNameToStgName.put(attrName, "s." + stgName);
            attributes.add(attrName);
            values.add("s." + stgName);
        }
        /*INSERT INTO Precore.PersonName(Attr1A, Attr1B, HeaderID, NameTypeID
        SELECT s.Attr1, s.Attr2, s.Attr3, h.HeaderID ,'Type Value' from Staging005.DataSet0005 s, Precore005.PersonHeader h
          where h.DataSetID=10 and h.SourceID=s.AttrX'
         */
        PrecoreSchema prec = new PrecoreSchema(userId);
        attributes.add("HeaderID");
        values.add("(SELECT HeaderID FROM "+prec.databaseName() +"."+ RefData.PERSON_HEADER.getName() +" WHERE DataSetID=" + ds.id + " AND SourceID=s." +headerMappings.iterator().next().getDataColumn().getName()  + ")");
        CoreSchemaTable tab = RefData.PRECORE_SCHEMA.getTable(tabType.getTableName());
        if (tab.getTypeTable() != null) {
            if (tabType.getAttributeType() == null) {
                throw new RuntimeException("Type expected but is null");
            }
            attributes.add(tab.getTypeTable().getIdColumnName());
            values.add("" + tab.getTypeTable().getTypeId(tabType.getAttributeType()));
        }
        StagingSchema stagingSchema = new StagingSchema(userId, ds.id);
        String sql = "INSERT INTO " + prec.databaseName() + "." + tabType.getTableName() +
                "(" + Joiner.on(",").join(attributes) + ") SELECT " + Joiner.on(",").join(values) +
                " FROM " + stagingSchema.databaseName() + "." + stagingSchema.dataSetTableName() + " s";

        if (tabType.getTableName().equals(RefData.PERSON_HEADER.getName())) {
            StringBuilder sb = new StringBuilder();
            sql = sb.append("INSERT INTO "+prec.databaseName() +"."+ RefData.PERSON_HEADER.getName() +"(DataSetID, SourceID, CreationTimestamp)")
                    .append("SELECT ").append(ds.id).append(", `").append(headerMappings.iterator().next().getDataColumn().getName()).append("`, NOW()")
                    .append("FROM `").append(stagingSchema.databaseName()).append("`.`").append(stagingSchema.dataSetTableName()).append("`")
                    .toString();
        }
        System.out.println("Precore SQL \n" + sql);
        Sql2o sql2o = new Sql2o(getDataSource());
        sql2o.createQuery(sql).executeUpdate();
    }

}
