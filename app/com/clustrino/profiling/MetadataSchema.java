package com.clustrino.profiling;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.clustrino.profiling.metadata.Column;
import com.clustrino.profiling.metadata.DataSet;
import com.clustrino.profiling.metadata.File;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class MetadataSchema {
    private final Long userId;
    private EbeanServer metadataEbeanServer;

    public MetadataSchema(Long userId) {
        this.userId = userId;
    }

    private String databaseName() {
        return String.format("metadata_%020d", this.userId);
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

    public String serverName() {
        return "/tmp/"+databaseName();
    }

    public EbeanServer medadataServer() {
        if (metadataEbeanServer != null) {
            return metadataEbeanServer;
        }
        ServerConfig config = new ServerConfig();
        config.setName(serverName());
        DataSourceConfig db = new DataSourceConfig();
        db.setDriver("com.mysql.jdbc.Driver");
        db.setUsername("root");
        db.setPassword("");
        db.setUrl("jdbc:mysql://localhost:3306/" + databaseName());
        db.setHeartbeatSql("select count(*) from clustrino.users");
        config.setDdlGenerate(true);
        config.setDdlRun(true);
        config.setDefaultServer(false);
        config.setRegister(true);
        config.setDataSourceConfig(db);
        config.setClasses(Arrays.asList(new Class<?>[] {DataSet.class, File.class, Column.class}));

        metadataEbeanServer = EbeanServerFactory.create(config);
        return metadataEbeanServer;
    }

    public void createTables() {
        Transaction t = medadataServer().beginTransaction();
        t.end();
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
            throw new PersistenceException("Failed to execute SQL", e);
        } finally {
            t.end();
        }
    }

    public static void doStuff() {
        ServerConfig config = new ServerConfig();
        config.setName("pgtest");
        DataSourceConfig db = new DataSourceConfig();
        db.setDriver("com.mysql.jdbc.Driver");
        db.setUsername("root");
        db.setPassword("");
        db.setUrl("jdbc:mysql://localhost:3306/clustrino");
        db.setHeartbeatSql("select count(*) from users");
        config.setDdlGenerate(false);
        config.setDdlRun(false);
        config.setDefaultServer(false);
        config.setRegister(false);
        config.setDataSourceConfig(db);
        EbeanServer server = EbeanServerFactory.create(config);
        Transaction t = server.createTransaction();
        try {
            Connection c = t.getConnection();
            String q = "drop table if exists Test2";
            PreparedStatement pstmt = c.prepareStatement(q);
            pstmt.execute();
            pstmt.close();
            q = "Create table Test2(id int primary key)";
            pstmt = c.prepareStatement(q);
            pstmt.execute();
            pstmt.close();
        } catch (SQLException e) {
            throw new PersistenceException("Failed to execute DDL", e);
        } finally {
            t.end();
        }

    }
}
