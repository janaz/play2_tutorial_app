package com.neutrino.data_loader;

import org.polyjdbc.core.PolyJDBC;
import org.polyjdbc.core.dialect.Dialect;
import org.polyjdbc.core.dialect.DialectRegistry;
import org.polyjdbc.core.schema.DDLQuery;
import org.polyjdbc.core.schema.SchemaManager;
import org.polyjdbc.core.schema.model.Schema;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoreSchema {
    private final List<CoreSchemaTable> tables = new ArrayList<>();
    private final int userId;
    private final String dbName;

    private CoreSchema(String dbName, int userId) {
        this.userId = userId;
        this.dbName = dbName;
    }

    public CoreSchema(String dbName) {
        this(dbName, -1);
    }

    private String fullDbName() {
        if (userId <= 0) {
            throw new IllegalStateException("User ID should be > 0");
        }
        return String.format("%s%03d", this.dbName, this.userId);
    }

    public CoreSchema forUser(int userId) {
        CoreSchema s = new CoreSchema(dbName, userId);
        for (CoreSchemaTable t : tables) {
            s.addTable(t.forDb(s.fullDbName()));
        }
        return s;
    }

    public CoreSchema addTable(CoreSchemaTable t) {
        tables.add(t);

        return this;
    }

    public void populate(DataSource ds) {
        for (CoreSchemaTable t : tables) {
            t.populateTypes(ds);
        }
    }

    public void create(DataSource ds) {
        if (userId <= 0) {
            throw new IllegalStateException("User ID should be > 0");
        }
        Dialect dialect = DialectRegistry.MYSQL.getDialect();
        PolyJDBC polyjdbc = new PolyJDBC(ds, dialect);
        SchemaManager schemaManager = null;
        try {
            List<String> ddls = new ArrayList<>();
            schemaManager = polyjdbc.schemaManager();

            Schema schema = new Schema(dialect);
            for (CoreSchemaTable t : tables) {
                ddls.addAll(t.updateSchema(schema));
            }

            schemaManager.create(schema);

            for (String ddl : ddls) {
                schemaManager.ddl(DDLQuery.ddl(ddl));
            }
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            polyjdbc.close(schemaManager);
        }

    }

    public CoreSchemaTable getTable(String tabName) {
        for (CoreSchemaTable t : tables) {
            if (tabName.equals(t.getName())){
                return t;
            }
        }
        return null;
    }
}
