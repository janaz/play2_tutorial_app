package com.clustrino.profiling;

import com.avaje.ebean.EbeanServer;
import com.clustrino.csv.CSVError;
import com.clustrino.csv.DataCategory;
import com.google.common.base.Joiner;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StagingSchema {
    private final Integer userId;
    private final Integer dataSetId;
    private EbeanServer stgSrv;

    public StagingSchema(Integer userId, Integer dataSetId) {
        this.userId = userId;
        this.dataSetId = dataSetId;
    }

    private String databaseName() {
        return String.format("Staging%03d", this.userId);
    }

    private String dataSetTableName() {
        return String.format("DataSet%05d", this.dataSetId);
    }

    private String rejectsTableName() {
        return String.format("FileRejects%05d", this.dataSetId);
    }

    public boolean isCreated() {
        return EbeanServerManager.getManager().isCreated(databaseName());
    }

    public EbeanServer server() {
        return EbeanServerManager.getManager().getServer(databaseName(), null, false);
    }

    public boolean createDatabase() {
        return EbeanServerManager.getManager().createDatabase(databaseName());
    }

    public void createTables(final List<DataCategory> categories) {
        if (dataSetId == null) {
            throw new IllegalArgumentException("DataSet id not passed");
        }
        dropDatasetTable();
        createDataSetTable(categories);
        dropRejectsTable();
        createRejectsTable();
    }

    public boolean insertIntoRejectsTable(final CSVError lastError) {
        return EbeanServerManager.getManager().executeQuery(server(), new QueryCallable<Boolean>() {
            @Override
            public Boolean call(PreparedStatement pstmt) throws SQLException {
                return pstmt.execute();
            }

            @Override
            public String getQuery() {
                StringBuilder sb = new StringBuilder();
                return sb.append("INSERT INTO ").append(rejectsTableName()).append("(line, content) values(?,?)").toString();
            }

            @Override
            public void setup(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, lastError.getLineNumber());
                pstmt.setString(2, lastError.getText());
            }
        });

    }

    public boolean insertIntoStagingTable(final List<DataCategory> categories, final List<?> values) {
        return EbeanServerManager.getManager().executeQuery(server(), new QueryCallable<Boolean>() {
            private final List<String> colValues = new ArrayList<>(values.size());

            @Override
            public Boolean call(PreparedStatement pstmt) throws SQLException {
                return pstmt.execute();
            }

            @Override
            public String getQuery() {
                final List<String> colNames = new ArrayList<>(categories.size());
                StringBuilder sb = new StringBuilder();
                List<String> colValuesPlaceholder = new ArrayList<>(values.size());
                if (categories.size() != values.size()) {
                    throw new RuntimeException("Lists should have the same number of elements");
                }
                for (int i = 0; i < categories.size(); i++) {
                    DataCategory cat = categories.get(i);
                    if (cat != DataCategory.UNKNOWN) {
                        String value = cat.dbValue(values.get(i));
                        colNames.add(cat.name());
                        colValues.add(value);
                        colValuesPlaceholder.add("?");
                    }
                }
                sb.append("INSERT INTO ").append(dataSetTableName()).append("(");
                sb.append(Joiner.on(',').join(colNames));
                sb.append(") VALUES (");
                sb.append(Joiner.on(',').join(colValuesPlaceholder));
                sb.append(")");
                return sb.toString();
            }

            @Override
            public void setup(PreparedStatement pstmt) throws SQLException {
                for (int i = 0; i < colValues.size(); i++) {
                    pstmt.setString(i + 1, colValues.get(i));
                }
            }
        });

    }

    private boolean createDataSetTable(final List<DataCategory> categories) {
        return EbeanServerManager.getManager().executeQuery(server(), new QueryCallable<Boolean>() {
            @Override
            public Boolean call(PreparedStatement pstmt) throws SQLException {
                return pstmt.execute();
            }

            @Override
            public String getQuery() {
                StringBuilder sb = new StringBuilder();
                sb.append("CREATE TABLE ").append(dataSetTableName()).append("(");
                sb.append("id bigint(20) NOT NULL AUTO_INCREMENT,");
                for (DataCategory cat : categories) {
                    if (cat != DataCategory.UNKNOWN) {
                        sb.append(cat.name()).append(" ").append(cat.dbType()).append(",");
                    }
                }
                sb.append("PRIMARY KEY (id) )");
                return sb.toString();
            }

            @Override
            public void setup(PreparedStatement pstmt) throws SQLException {
            }
        });

    }

    private boolean dropTable(final String tableName) {
        return EbeanServerManager.getManager().executeQuery(server(), new QueryCallable<Boolean>() {
            @Override
            public Boolean call(PreparedStatement pstmt) throws SQLException {
                return pstmt.execute();
            }

            @Override
            public String getQuery() {
                return "DROP TABLE IF EXISTS " + tableName;
            }

            @Override
            public void setup(PreparedStatement pstmt) throws SQLException {
            }
        });

    }
    private boolean dropDatasetTable() {
        return dropTable(dataSetTableName());
    }

    private boolean dropRejectsTable() {
        return dropTable(rejectsTableName());
    }

    private boolean createRejectsTable() {
        return EbeanServerManager.getManager().executeQuery(server(), new QueryCallable<Boolean>() {
            @Override
            public Boolean call(PreparedStatement pstmt) throws SQLException {
                return pstmt.execute();
            }

            @Override
            public String getQuery() {
                StringBuilder sb = new StringBuilder();
                sb.append("CREATE TABLE ").append(rejectsTableName()).append("(");
                sb.append("id bigint(20) NOT NULL AUTO_INCREMENT,");
                sb.append("line bigint(20),");
                sb.append("content text,");
                sb.append("PRIMARY KEY (id) )");
                return sb.toString();
            }

            @Override
            public void setup(PreparedStatement pstmt) throws SQLException {
            }
        });

    }

}
