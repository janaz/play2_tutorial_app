package com.neutrino.profiling;

import com.avaje.ebean.EbeanServer;
import com.google.common.base.Joiner;
import com.neutrino.csv.CSVDataHeader;
import com.neutrino.csv.CSVError;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StagingSchema {
    private final Integer userId;
    private final Integer dataSetId;

    public StagingSchema(Integer userId, Integer dataSetId) {
        this.userId = userId;
        this.dataSetId = dataSetId;
    }

    public String databaseName() {
        return String.format("Staging%03d", this.userId);
    }

    public String dataSetTableName() {
        return String.format("DataSet%05d", this.dataSetId);
    }

    public String rejectsTableName() {
        return String.format("FileRejects%05d", this.dataSetId);
    }

    public boolean isCreated() {
        return EbeanServerManager.getManager().isCreated(databaseName());
    }

    public EbeanServer server() {
        return EbeanServerManager.getManager().getMysqlServer();
    }

    public boolean createDatabase() {
        return EbeanServerManager.getManager().createDatabase(databaseName());
    }

    public void createTables(final List<CSVDataHeader> headers) {
        if (dataSetId == null) {
            throw new IllegalArgumentException("DataSet id not passed");
        }
        dropDatasetTable();
        createDataSetTable(headers);
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
                return sb.append("INSERT INTO ").append(databaseName()).append(".").append(rejectsTableName()).append("(Line, Content) values(?,?)").toString();
            }

            @Override
            public void setup(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, lastError.getLineNumber());
                pstmt.setString(2, lastError.getText());
            }
        });

    }

    public boolean insertIntoStagingTable(final List<CSVDataHeader> headers, final List<?> values) {
        final List<String> colValues = new ArrayList<>(values.size());

        return EbeanServerManager.getManager().executeQuery(server(), new QueryCallable<Boolean>() {
            @Override
            public Boolean call(PreparedStatement pstmt) throws SQLException {
                return pstmt.execute();
            }

            @Override
            public String getQuery() {
                final List<String> colNames = new ArrayList<>(headers.size());
                StringBuilder sb = new StringBuilder();
                List<String> colValuesPlaceholder = new ArrayList<>(values.size());
                if (headers.size() != values.size()) {
                    throw new RuntimeException("Lists should have the same number of elements");
                }
                for (int i = 0; i < headers.size(); i++) {
                    CSVDataHeader header = headers.get(i);
                    String value = header.dbValue(values.get(i));
                    colNames.add(header.name());
                    colValues.add(value);
                    colValuesPlaceholder.add("?");
                }
                sb.append("INSERT INTO ").append(databaseName()).append(".").append(dataSetTableName()).append("(");
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

    private boolean createDataSetTable(final List<CSVDataHeader> headers) {
        return EbeanServerManager.getManager().executeQuery(server(), new QueryCallable<Boolean>() {
            @Override
            public Boolean call(PreparedStatement pstmt) throws SQLException {
                return pstmt.execute();
            }

            @Override
            public String getQuery() {
                StringBuilder sb = new StringBuilder();
                sb.append("CREATE TABLE ").append(databaseName()).append(".").append(dataSetTableName()).append("(");
                sb.append("ID bigint(20) NOT NULL AUTO_INCREMENT,");
                for (CSVDataHeader cat : headers) {
                    sb.append(cat.name()).append(" ").append(cat.dbType()).append(",");
                }
                sb.append("PRIMARY KEY (ID) )");
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
                return "DROP TABLE IF EXISTS " + databaseName() + "." + tableName;
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
                sb.append("CREATE TABLE ").append(databaseName()).append(".").append(rejectsTableName()).append("(");
                sb.append("ID bigint(20) NOT NULL AUTO_INCREMENT,");
                sb.append("Line bigint(20),");
                sb.append("Content text,");
                sb.append("PRIMARY KEY (ID) )");
                return sb.toString();
            }

            @Override
            public void setup(PreparedStatement pstmt) throws SQLException {
            }
        });

    }

}
