package com.neutrino.data_loader;

import org.polyjdbc.core.PolyJDBC;
import org.polyjdbc.core.dialect.Dialect;
import org.polyjdbc.core.dialect.DialectRegistry;
import org.polyjdbc.core.schema.DDLQuery;
import org.polyjdbc.core.schema.SchemaManager;
import org.polyjdbc.core.schema.model.Schema;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tomasz.janowski on 31/03/14.
 */
public class CoreSchema {
    private final List<CoreSchemaTable> tables = new ArrayList<>();
    private final int userId;

    private CoreSchema(int userId) {
        this.userId = userId;
    }

    public CoreSchema() {
        this(-1);
    }

    public CoreSchema forUser(int userId) {
        CoreSchema s = new CoreSchema(userId);
        for (CoreSchemaTable t: tables) {
            s.addTable(t.forUser(userId));
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
        } finally {
            polyjdbc.close(schemaManager);
        }

    }

}
