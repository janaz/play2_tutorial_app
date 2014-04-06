package com.neutrino.profiling;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.ServerConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.polyjdbc.core.PolyJDBC;
import org.polyjdbc.core.dialect.Dialect;
import org.polyjdbc.core.dialect.DialectRegistry;
import org.polyjdbc.core.schema.DDLQuery;
import org.polyjdbc.core.schema.SchemaManager;
import org.sql2o.Sql2o;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EbeanServerManager {
    private final static EbeanServerManager INSTANCE = new EbeanServerManager();
    private static final ConcurrentMap<String, EbeanServer> SERVERS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, DataSource> DATA_SOURCES = new ConcurrentHashMap<>();

    private EbeanServerManager() {

    }

    public static EbeanServerManager getManager() {
        return INSTANCE;
    }

    public static DataSource dataSource() {
        return dataSource("mysql");
    }

    private static DataSource c3p0DS(String dbName) {
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        try {
            cpds.setDriverClass("com.mysql.jdbc.Driver");
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }

        cpds.setJdbcUrl("jdbc:mysql://localhost:3306/" + dbName + "?useUnicode=yes&characterEncoding=UTF8&sessionVariables=storage_engine=InnoDB");
        cpds.setUser("root");
        cpds.setPassword("");
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
        cpds.setPreferredTestQuery("select 1");
        cpds.setIdleConnectionTestPeriod(60);
        return cpds;
    }

    private static DataSource bonecpDS(String dbName) {

        BoneCPDataSource ds = new BoneCPDataSource();
        ds.setDriverClass("com.mysql.jdbc.Driver");
        ds.setJdbcUrl("jdbc:mysql://localhost:3306/" + dbName + "?useUnicode=yes&characterEncoding=UTF8&sessionVariables=storage_engine=InnoDB");
        ds.setUsername("root");
        ds.setPassword("");
        ds.setMaxConnectionAgeInSeconds(3600);
        ds.setMinConnectionsPerPartition(5);
        ds.setMaxConnectionsPerPartition(10);
        ds.setPartitionCount(1);
        ds.setConnectionTestStatement("select 1");
        ds.setIdleConnectionTestPeriodInMinutes(1);
        ds.setStatementsCacheSize(20);
        return ds;
    }

    public static DataSource dataSource(String dbName) {
        if (!DATA_SOURCES.containsKey(dbName)) {
            DataSource ds = bonecpDS(dbName);
            DATA_SOURCES.putIfAbsent(dbName, ds);
        }
        return DATA_SOURCES.get(dbName);
    }

    public boolean isCreated(final String dbName) {
        String sql = "SHOW DATABASES LIKE :dbName";

        List<String> results = new Sql2o(dataSource()).createQuery(sql).addParameter("dbName", dbName).executeScalarList(String.class);
        return results.size() > 0;
    }

    public boolean createDatabase(final String dbName) {
        Dialect dialect = DialectRegistry.MYSQL.getDialect();
        PolyJDBC polyjdbc = new PolyJDBC(dataSource(), dialect);
        SchemaManager schemaManager = null;
        try {
            schemaManager = polyjdbc.schemaManager();
            schemaManager.ddl(DDLQuery.ddl("CREATE DATABASE `" + dbName + "`"));
            schemaManager.ddl(DDLQuery.ddl("ALTER DATABASE `" + dbName + "` DEFAULT CHARACTER SET = 'UTF8'"));
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }  finally {
            polyjdbc.close(schemaManager);
        }
    }

    public <V> V executeQuery(EbeanServer s, QueryCallable<V> query) {
        PreparedStatement pstmt = null;
        Transaction t = s.createTransaction();
        try {
            Connection conn = t.getConnection();
            pstmt = conn.prepareStatement(query.getQuery());
            query.setup(pstmt);
            return query.call(pstmt);
        } catch (SQLException e) {
            throw new PersistenceException("Failed to execute SQL " + query, e);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            t.commit();
            t.end();
        }
    }

    private String serverName(String dbName) {
        return "server_" + dbName;
    }

    public EbeanServer getServer(String dbName, List<Class<?>> classes, boolean runDdl) {
        return getServer(dbName, classes, runDdl, false);
    }

    public EbeanServer getDefaultServer() {
        List<Class<?>> classes = Collections.emptyList();
        return getServer("Configuration", classes , false, true);
    }


    public EbeanServer getServer(String dbName, List<Class<?>> classes, boolean runDdl, boolean isDefault) {
        if (isDefault || runDdl || !SERVERS.containsKey(dbName)) {
            ServerConfig config = new ServerConfig();
            config.setName(serverName(dbName));
            config.setDatabaseBooleanTrue("T");
            config.setDatabaseBooleanFalse("F");
            config.setDefaultServer(isDefault);
            config.setRegister(true);
            config.setEnhanceLogLevel(10);
            config.setDataSource(dataSource(dbName));
            if (runDdl) {
                config.setDdlGenerate(true);
                config.setDdlRun(true);
                config.setRegister(false);
            }
            if (classes != null) {
                config.setClasses(classes);
            }
            SERVERS.putIfAbsent(dbName, EbeanServerFactory.create(config));
        }
        return SERVERS.get(dbName);
    }

}
