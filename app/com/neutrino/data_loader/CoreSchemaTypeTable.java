package com.neutrino.data_loader;

import com.neutrino.profiling.EbeanServerManager;
import org.polyjdbc.core.PolyJDBC;
import org.polyjdbc.core.dialect.Dialect;
import org.polyjdbc.core.dialect.DialectRegistry;
import org.polyjdbc.core.query.InsertQuery;
import org.polyjdbc.core.query.QueryFactory;
import org.polyjdbc.core.schema.model.Schema;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreSchemaTypeTable {
    private final CoreSchemaTable underlyingTable;
    private final Map<Integer, String> values = new HashMap<>();

    private final String idColumnName;

    public CoreSchemaTypeTable(String name, String idColumnName) {
        this(new CoreSchemaTable(name), idColumnName);
    }

    private CoreSchemaTypeTable(CoreSchemaTable underlyingTable, String idColumnName) {
        this.underlyingTable = underlyingTable;
        this.idColumnName = idColumnName;
        underlyingTable.addColumn(new CoreSchemaColumn(idColumnName, "INTEGER").id());
        underlyingTable.addColumn(new CoreSchemaColumn("Type", "VARCHAR", 30).notNull().unique());
    }

    public String getIdColumnName() {
        return idColumnName;
    }


    public CoreSchemaTypeTable addValue(int id, String value) {
        values.put(id, value);
        return this;
    }

    public String getValue(int id) {
        return values.get(id);
    }

    public String getName() {
        return underlyingTable.getName();
    }

    public CoreSchemaTable table() {
        return underlyingTable;
    }

    public List<String> updateSchema(Schema schema) {
        return underlyingTable.updateSchema(schema);
    }

    public CoreSchemaTypeTable forUser(int userId) {
        CoreSchemaTable newUnderlaying = underlyingTable.forUser(userId);
        CoreSchemaTypeTable newTable = new CoreSchemaTypeTable(newUnderlaying, idColumnName);
        for (Integer id : values.keySet()) {
            newTable.addValue(id, values.get(id));
        }
        return newTable;
    }

    public void populate(DataSource ds) {
        Dialect dialect = DialectRegistry.MYSQL.getDialect();
        PolyJDBC polyjdbc = new PolyJDBC(ds, dialect);
        for (Integer id : values.keySet()) {
            InsertQuery q = QueryFactory.insert().into(underlyingTable.fullTableName());
            q = q.value(idColumnName, id).value("Type", values.get(id));
            System.out.println(q.toString());
            polyjdbc.simpleQueryRunner().insert(q);
        }

    }
}
