package com.clustrino.profiling;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.clustrino.csv.CSVError;
import com.clustrino.csv.DataCategory;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StagingSchema {
    private final Long userId;
    private final Long dataSetId;
    private EbeanServer stgSrv;

    public StagingSchema(Long userId, Long dataSetId) {
        this.userId = userId;
        this.dataSetId = dataSetId;
    }

    private String databaseName() {
        return String.format("Staging%020d", this.userId);
    }

    private String dataSetTableName() {
        return String.format("DataSet%020d", this.dataSetId);
    }

    private String rejectsTableName() {
        return String.format("FileRejects%020d", this.dataSetId);
    }

    private EbeanServer ebeanServer() {
        ServerConfig config = new ServerConfig();
        config.setName("pgtest");
        DataSourceConfig db = new DataSourceConfig();
        db.setDriver("com.mysql.jdbc.Driver");
        db.setUsername("root");
        db.setPassword("");
        db.setUrl("jdbc:mysql://localhost:3306/mysql");
        db.setHeartbeatSql("select count(*) from clustrino.users");
        config.setDdlGenerate(false);
        config.setDdlRun(false);
        config.setDefaultServer(false);
        config.setRegister(false);
        config.setDataSourceConfig(db);
        return EbeanServerFactory.create(config);
    }

    public boolean isCreated() {
        String query = "show databases like ?";
        Transaction t = ebeanServer().createTransaction();
        try {
            Connection c = t.getConnection();
            PreparedStatement pstmt = c.prepareStatement(query);
            pstmt.setString(1, databaseName());
            ResultSet r = pstmt.executeQuery();
            boolean created = r.next();
            pstmt.close();
            return created;
        } catch (SQLException e) {
            throw new PersistenceException("Failed to execute SQL", e);
        } finally {
            t.end();
        }
    }

    private EbeanServer ebeanStagingServer() {
        if (stgSrv == null) {
            ServerConfig config = new ServerConfig();
            config.setName("/tmp/"+databaseName());
            DataSourceConfig db = new DataSourceConfig();
            db.setDriver("com.mysql.jdbc.Driver");
            db.setUsername("root");
            db.setPassword("");
            db.setUrl("jdbc:mysql://localhost:3306/" + databaseName());
            db.setHeartbeatSql("select count(*) from clustrino.users");
            config.setDdlGenerate(false);
            config.setDdlRun(false);
            config.setDefaultServer(false);
            config.setRegister(false);
            config.setDataSourceConfig(db);

            stgSrv = EbeanServerFactory.create(config);
        }
        return stgSrv;
    }

    public boolean createDatabase() {
        String query = "create database "+ databaseName();
        Transaction t = ebeanServer().createTransaction();
        try {
            Connection c = t.getConnection();
            PreparedStatement pstmt = c.prepareStatement(query);
            boolean ret = pstmt.execute();
            pstmt.close();
            return ret;
        } catch (SQLException e) {
            throw new PersistenceException("Failed to execute SQL " + query, e);
        } finally {
            t.end();
        }
    }
    public boolean insertIntoRejectsTable(CSVError lastError) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(rejectsTableName()).append("(line, content) values(?,?)");
        Transaction t = ebeanStagingServer().createTransaction();

        try {
            Connection c = t.getConnection();
            PreparedStatement pstmt = c.prepareStatement(sb.toString());
            pstmt.setLong(1, lastError.getLineNumber());
            pstmt.setString(2, lastError.getText());
            boolean res = pstmt.execute();
            pstmt.close();
            return res;
        } catch (SQLException e) {
            throw new PersistenceException("Failed to execute SQL " + sb, e);
        } finally {
            t.commit();
            t.end();
        }
    }

    public boolean insertIntoStagingTable(List<DataCategory> categories, List<?> values) {
        //if (1 == 2-1) return true;
        StringBuilder sb = new StringBuilder();
        List<String> colNames = new ArrayList<>(categories.size());
        List<String> colValues = new ArrayList<>(values.size());
        List<String> colValuesPlaceholder = new ArrayList<>(values.size());
        if (categories.size() != values.size()) {
            throw new RuntimeException("Lists should have the same number of elements");
        }
        for (int i = 0; i<categories.size();i++) {
            DataCategory cat = categories.get(i);
            if ( cat != DataCategory.UNKNOWN) {
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
        Transaction t = ebeanStagingServer().createTransaction();
        try {
            Connection c = t.getConnection();
            PreparedStatement pstmt = c.prepareStatement(sb.toString());
            for (int i=0;i<colValues.size();i++) {
                pstmt.setString(i+1, colValues.get(i));
            }
            boolean res = pstmt.execute();
            pstmt.close();
            return res;
        } catch (SQLException e) {
            throw new PersistenceException("Failed to execute SQL " + sb, e);
        } finally {
            t.commit();
            t.end();
        }
    }

    public boolean createStagingTable(List<DataCategory> categories) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(dataSetTableName()).append("(");
        sb.append("id bigint(20) NOT NULL AUTO_INCREMENT,");
        for (DataCategory cat : categories) {
            if (cat != DataCategory.UNKNOWN) {
                sb.append(cat.name()).append(" ").append(cat.dbType()).append(",");
            }
        }
        sb.append("PRIMARY KEY (id) )");
        Transaction t = ebeanStagingServer().createTransaction();
        try {
            Connection c = t.getConnection();
            PreparedStatement pstmt = c.prepareStatement("DROP TABLE IF EXISTS "+dataSetTableName());
            pstmt.execute();
            pstmt.close();
            PreparedStatement pstmt2 = c.prepareStatement(sb.toString());
            boolean ret = pstmt2.execute();
            pstmt2.close();
            return ret;
        } catch (SQLException e) {
            throw new PersistenceException("Failed to execute SQL " + sb, e);
        } finally {
            t.end();
        }
    }

    public boolean createRejectsTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(rejectsTableName()).append("(");
        sb.append("id bigint(20) NOT NULL AUTO_INCREMENT,");
        sb.append("line bigint(20),");
        sb.append("content text,");
        sb.append("PRIMARY KEY (id) )");
        Transaction t = ebeanStagingServer().createTransaction();
        try {
            Connection c = t.getConnection();
            PreparedStatement pstmt = c.prepareStatement("DROP TABLE IF EXISTS "+rejectsTableName());
            pstmt.execute();
            pstmt.close();
            PreparedStatement pstmt2 = c.prepareStatement(sb.toString());
            boolean ret = pstmt2.execute();
            pstmt2.close();
            return ret;
        } catch (SQLException e) {
            throw new PersistenceException("Failed to execute SQL " + sb, e);
        } finally {
            t.end();
        }
    }

}
