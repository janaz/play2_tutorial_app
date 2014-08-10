package com.neutrino.data_loader;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.neutrino.datamappingdiscovery.CollectionUtils;
import com.neutrino.models.metadata.ColumnMapping;
import com.neutrino.models.metadata.DataSet;
import com.neutrino.profiling.EbeanServerManager;
import com.neutrino.profiling.MetadataSchema;
import com.neutrino.profiling.PrecoreSchema;
import com.neutrino.profiling.StagingSchema;
import org.sql2o.Sql2o;

import javax.annotation.Nullable;
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
        CoreSchema mySchema = new CoreSchema("PreCore");
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
        MetadataSchema met = new MetadataSchema(userId);
        for (DataSet ds : dataSets()) {
            ds.setState(DataSet.State.DATA_LOADING_STARTED);
            ds.save(met.server().getName());
            List<ColumnMapping> dsMappings = ds.getMappings();
            if (dsMappings.isEmpty()) {
                ds.setState(DataSet.State.DATA_LOADING_DONE);
                ds.save(met.server().getName());
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
            ds.setState(DataSet.State.DATA_LOADING_DONE);
            ds.save(met.server().getName());

        }
    }

    private void processTableType(final DataSet ds, TableType tabType, Collection<ColumnMapping> headerMappings, Collection<ColumnMapping> tabTypeMappings) {
        Map<String, String> attrNameToStgName = new HashMap<>();
        List<String> attributes = new ArrayList<>();
        List<String> values1 = new ArrayList<>();
        List<String> values2 = new ArrayList<>();
        List<String> values3 = new ArrayList<>();
        List<String> values4 = new ArrayList<>();
        List<String> froms = new ArrayList<>();
        StagingSchema stagingSchema = new StagingSchema(userId, ds.id);
        froms.add(stagingSchema.databaseName() + "." + stagingSchema.dataSetTableName() + " s");
        int subTabCount = 0;
        Map<String, Collection<ColumnMapping>> byAttributeName = CollectionUtils.listAsMap(tabTypeMappings, new CollectionUtils.ListToMapConverter<String, ColumnMapping>() {
            @Override
            public String getKey(ColumnMapping item) {
                if (item.getCoreAttributeName() == null) {
                    if (item.getCoreTableName().equals(RefData.PERSON_NAME.getName())) {
                        return RefData.FULL_NAME_COLUMN;
                    } else if (item.getCoreTableName().equals(RefData.PERSON_ADDRESS.getName())) {
                        return RefData.FULL_ADDRESS_LINE_COLUMN;
                    } else {
                        throw new RuntimeException("Attribute Name null for dataset " + ds.id + " table name" + item.getCoreTableName());
                    }
                } else {
                    return item.getCoreAttributeName();
                }
            }
        });

        if (tabType.getTableName().equals(RefData.PERSON_NAME)) {
            if (!byAttributeName.containsKey(RefData.FULL_NAME_COLUMN)) {
                byAttributeName.put(RefData.FULL_NAME_COLUMN, null);
            }
        } else if (tabType.getTableName().equals(RefData.PERSON_ADDRESS)) {
            if (!byAttributeName.containsKey(RefData.FULL_ADDRESS_LINE_COLUMN)) {
                byAttributeName.put(RefData.FULL_ADDRESS_LINE_COLUMN, null);
            }
            if (!byAttributeName.containsKey(RefData.FULL_AREA_COLUMN)) {
                byAttributeName.put(RefData.FULL_AREA_COLUMN, null);
            }

        }
        for (String attrName : byAttributeName.keySet()) {
            Collection<ColumnMapping> mappings = byAttributeName.get(attrName);
            String stgName;

            if (!attrName.equals(RefData.FULL_AREA_COLUMN) &&
                    !attrName.equals(RefData.FULL_ADDRESS_LINE_COLUMN) &&
                    !attrName.equals(RefData.FULL_NAME_COLUMN)) {
                ColumnMapping firstMapping = mappings.iterator().next();
                if (!tabType.getTableName().equals(firstMapping.getCoreTableName())) {
                    throw new RuntimeException("Table Name mismatch1!!! " + tabType.getTableName() + "<->" + firstMapping.getCoreTableName());
                }
                stgName = firstMapping.getDataColumn().getName();
                values1.add("upper(trim(s." + stgName + ")) as " + stgName);
            } else {
                stgName = attrName + "_multi";
                List<String> stagingNames = new ArrayList<>();
                List<String> stagingNamesAddrPrefix = new ArrayList<>();
                String[] candidatesAttrs;
                if (attrName.equals(RefData.FULL_NAME_COLUMN)) {
                    String[] candidates = new String[]{"Salutation", "FirstName", "MiddleName", "Generation", "Surname", "Suffix"};
                    stagingNames.addAll(buildFullColumnStagingList(candidates, byAttributeName));
                } else if (attrName.equals(RefData.FULL_AREA_COLUMN)) {
                    String[] candidates = new String[]{"Suburb", "State", "Country"};
                    stagingNames.addAll(buildFullColumnStagingList(candidates, byAttributeName));
                } else if (attrName.equals(RefData.FULL_ADDRESS_LINE_COLUMN)) {
                    String[] candidates1 = new String[]{"FloorType", "FloorNumber", "UnitNumber"};
                    stagingNamesAddrPrefix.addAll(buildFullColumnStagingList(candidates1, byAttributeName));
                    String[] candidates2 = new String[]{"HouseNumber", "HouseNumberSuffix", "StreetName", "StreetType",
                            "StreetSuffix", "BuildingName", "PostalDelType", "PostalDelNumber",
                            "PostalDelSuffix"};
                    stagingNames.addAll(buildFullColumnStagingList(candidates2, byAttributeName));
                }
                if (mappings != null) {
                    Collection<String> stagingNamesColl = Collections2.transform(mappings, new Function<ColumnMapping, String>() {
                        @Nullable
                        @Override
                        public String apply(@Nullable ColumnMapping columnMapping) {
                            return columnMapping.getDataColumn().getName();
                        }
                    });
                    stagingNames.addAll(Lists.newArrayList(stagingNamesColl));
                }
                List<String> withUpperAddrPrefix = Lists.transform(stagingNamesAddrPrefix, new Function<String, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable String stg) {
                        return "upper(trim(s." + stg + "))";
                    }
                });

                List<String> withUpper = Lists.transform(stagingNames, new Function<String, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable String stg) {
                        return "upper(trim(s." + stg + "))";
                    }
                });
                String withUpperCommaList = Joiner.on(",").join(withUpper);
                String withUpperAddrPrefixCommaList = Joiner.on(",").join(withUpperAddrPrefix);
                if (withUpperAddrPrefix.size() > 0) {
                    values1.add("upper(trim(" +
                            "CONCAT_WS('/',CONCAT_WS(' ', " + withUpperAddrPrefixCommaList + ")," +
                            "CONCAT_WS(' ', " + withUpperCommaList + ")" +
                            "))) as " + stgName);
                } else {
                    values1.add("upper(trim(CONCAT_WS(' ', " + withUpperCommaList + "))) as " + stgName);
                }
            }

            if (attrName.equals(RefData.EMAIL_ADDRESS_COLUMN)) {
                if (mappings.size() != 1) {
                    throw new RuntimeException("Mapping count should be 1 for " + attrName);
                }
                attributes.add(attrName);
                String colExp = "if (" + stgName + " regexp '^[A-Z0-9._%+-]+@[A-Z0-9.-]+[.][A-Z]{2,}$' = 1, " + stgName + ", null)";
                values2.add(colExp + " as " + stgName);
                values3.add(stgName);
                values4.add(stgName);
            } else if (attrName.endsWith("Date")) {
                if (mappings.size() != 1) {
                    throw new RuntimeException("Mapping count should be 1 for " + attrName);
                }
                subTabCount++;
                String subTabName = "t" + subTabCount;
                String firstTwoDigName = "FirstTwoDigits" + subTabCount;
                String lastTwoDigName = "LastTwoDigits" + subTabCount;
                String formatName = "Format" + subTabCount;
                String subTab = "(select " +
                        "       max(substr(trim(" + stgName + "), 1, 2)) as FirstTwoDigits,\n" +
                        "       max(substr(trim(" + stgName + "), CHAR_LENGTH(trim(" + stgName + ")) - 1, 2)) as LastTwoDigits\n" +
                        "     from " + stagingSchema.databaseName() + "." + stagingSchema.dataSetTableName() + ") " + subTabName;
                froms.add(subTab);
                attributes.add(attrName);
                values1.add(subTabName + ".FirstTwoDigits as " + firstTwoDigName);
                values1.add(subTabName + ".LastTwoDigits as " + lastTwoDigName);
                values2.add(stgName);
                values2.add(firstTwoDigName);
                values2.add(lastTwoDigName);
                values2.add(Cleansing.FORMAT_VALUE_EXPR_LEVEL_2.replaceAll("%COLUMN_NAME%", stgName) + " as " + formatName);
                values3.add(Cleansing.DATE_VALUE_EXPR_LEVEL_3.replaceAll("%FORMAT%", formatName).
                        replaceAll("%LAST_TWO_DIGITS%", lastTwoDigName).
                        replaceAll("%FIRST_TWO_DIGITS%", firstTwoDigName).
                        replaceAll("%COLUMN_NAME%", stgName) + " as " + stgName);
                values4.add(stgName);
            } else if (attrName.equals(RefData.PHONE_NUMBER_COLUMN)) {
                if (mappings.size() != 1) {
                    throw new RuntimeException("Mapping count should be 1 for " + attrName);
                }

                String extName = "EXT_" + stgName;
                String stringLength = "SLEN_" + stgName;
                String leadingPlusFlag = "LPF_" + stgName;
                String leadingZeroFlag = "LZF_" + stgName;
                String leadingDblZeroFlag = "LDZF_" + stgName;

                values2.add(Cleansing.PHONE_VALUE_EXPR_2.replaceAll("%COLUMN_NAME%", stgName) + " as " + stgName);
                values2.add(Cleansing.PHONE_EXT_VALUE_EXPR_2.replaceAll("%COLUMN_NAME%", stgName) + " as " + extName);

                values3.add("replace(" + stgName + ", '+', '') as " + stgName);
                values3.add("length(replace(" + stgName + ", '+', '')) as " + stringLength);
                values3.add("if (" + stgName + " like '+%', 1, 0) as " + leadingPlusFlag);
                values3.add("if (" + stgName + " like '0%', 1, 0) as " + leadingZeroFlag);
                values3.add("if (" + stgName + " like '00%', 1, 0) as " + leadingDblZeroFlag);
                values3.add(extName);

                attributes.add(RefData.COUNTRY_CODE_COLUMN);
                values4.add(Cleansing.COUNTRY_CODE_VAL_4.replaceAll("%COLUMN_NAME%", stgName).
                        replaceAll("%STR_LEN%", stringLength).
                        replaceAll("%LPF%", leadingPlusFlag).
                        replaceAll("%LZF%", leadingZeroFlag).
                        replaceAll("%LDZF%", leadingDblZeroFlag) + " as CC_" + stgName);

                attributes.add(RefData.AREA_CODE_COLUMN);
                values4.add(Cleansing.AREA_CODE_VAL_4.replaceAll("%COLUMN_NAME%", stgName).
                        replaceAll("%STR_LEN%", stringLength).
                        replaceAll("%LPF%", leadingPlusFlag).
                        replaceAll("%LZF%", leadingZeroFlag).
                        replaceAll("%LDZF%", leadingDblZeroFlag) + " as AC_" + stgName);

                attributes.add(RefData.PHONE_NUMBER_COLUMN);
                values4.add(Cleansing.PHONE_NUMBER_VAL_4.replaceAll("%COLUMN_NAME%", stgName).
                        replaceAll("%STR_LEN%", stringLength).
                        replaceAll("%LPF%", leadingPlusFlag).
                        replaceAll("%LZF%", leadingZeroFlag).
                        replaceAll("%LDZF%", leadingDblZeroFlag) + " as PN_" + stgName);

                attributes.add(RefData.EXTENSION_COLUMN);
                values4.add(extName);
            } else {
                attributes.add(attrName);
                values2.add(stgName);
                values3.add(stgName);
                values4.add(stgName);
            }
        }
        /*INSERT INTO Precore.PersonName(Attr1A, Attr1B, HeaderID, NameTypeID
        SELECT s.Attr1, s.Attr2, s.Attr3, h.HeaderID ,'Type Value' from Staging005.DataSet0005 s, Precore005.PersonHeader h
          where h.DataSetID=10 and h.SourceID=s.AttrX'
         */
        PrecoreSchema prec = new PrecoreSchema(userId);
        attributes.add("HeaderID");
        values1.add("(SELECT HeaderID FROM " + prec.databaseName() + "." + RefData.PERSON_HEADER.getName() + " WHERE DataSetID=" + ds.id + " AND SourceID=s." + headerMappings.iterator().next().getDataColumn().getName() + ") as _HeaderID");
        values2.add("_HeaderID");
        values3.add("_HeaderID");
        values4.add("_HeaderID");
        CoreSchemaTable tab = RefData.PRECORE_SCHEMA.getTable(tabType.getTableName());
        if (tab.getTypeTable() != null) {
            if (tabType.getAttributeType() == null) {
                throw new RuntimeException("Type expected but is null");
            }
            attributes.add(tab.getTypeTable().getIdColumnName());
            values1.add("" + tab.getTypeTable().getTypeId(tabType.getAttributeType()) + " as _TypeId");
            values2.add("_TypeId");
            values3.add("_TypeId");
            values4.add("_TypeId");
        }
        String insertInto = "INSERT INTO " + prec.databaseName() + "." + tabType.getTableName() +
                "(" + Joiner.on(",\n").join(attributes) + ")\n";
        String select1 = " SELECT " + Joiner.on(",\n").join(values1) +
                " FROM " + Joiner.on(",\n").join(froms);
        String select2 = " SELECT " + Joiner.on(",\n").join(values2) + " FROM (" + select1 + ") a \n";
        String select3 = " SELECT " + Joiner.on(",\n").join(values3) + " FROM (" + select2 + ") b \n";
        String select4 = " SELECT " + Joiner.on(",\n").join(values4) + " FROM (" + select3 + ") c \n";
        String sql = insertInto + select4;
        System.out.println(sql);
        if (tabType.getTableName().equals(RefData.PERSON_HEADER.getName())) {
            StringBuilder sb = new StringBuilder();
            sql = sb.append("INSERT INTO " + prec.databaseName() + "." + RefData.PERSON_HEADER.getName() + "(DataSetID, SourceID, CreationTimestamp) ")
                    .append("SELECT ").append(ds.id).append(", `").append(headerMappings.iterator().next().getDataColumn().getName()).append("`, NOW() ")
                    .append("FROM `").append(stagingSchema.databaseName()).append("`.`").append(stagingSchema.dataSetTableName()).append("`")
                    .toString();
        }
        System.out.println("Precore SQL \n" + sql);
        Sql2o sql2o = new Sql2o(getDataSource());
        sql2o.createQuery(sql).executeUpdate();
    }

    private List<String> buildFullColumnStagingList(String[] candidatesAttrs, Map<String, Collection<ColumnMapping>> byAttributeName) {
        List<String> out = new ArrayList<>();
        for (String attr : candidatesAttrs) {
            Collection<ColumnMapping> mappings = byAttributeName.get(attr);
            if (mappings != null && mappings.size() == 1) {
                out.add(mappings.iterator().next().getDataColumn().getName());
            }
        }
        return out;
    }

}
