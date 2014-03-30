package com.neutrino.profiling;

import com.google.common.base.Joiner;
import com.neutrino.csv.CSVDataHeader;
import com.neutrino.csv.CSVError;
import com.neutrino.csv.CSVLine;
import org.polyjdbc.core.PolyJDBC;
import org.polyjdbc.core.dialect.Dialect;
import org.polyjdbc.core.dialect.DialectRegistry;
import org.polyjdbc.core.query.InsertQuery;
import org.polyjdbc.core.query.QueryFactory;
import org.polyjdbc.core.schema.SchemaManager;
import org.polyjdbc.core.schema.model.RelationBuilder;
import org.polyjdbc.core.schema.model.Schema;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.StatementRunnableWithResult;

import java.sql.BatchUpdateException;
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

    public boolean createDatabase() {
        return EbeanServerManager.getManager().createDatabase(databaseName());
    }

    public void createTables(final List<CSVDataHeader> headers) {
        if (dataSetId == null) {
            throw new IllegalArgumentException("DataSet id not passed");
        }
        createDataSetTable(headers);
        createRejectsTable();
    }

    public boolean insertIntoRejectsTable(final CSVError lastError) {
        Dialect dialect = DialectRegistry.MYSQL.getDialect();
        PolyJDBC polyjdbc = new PolyJDBC(EbeanServerManager.getManager().dataSource(), dialect);
        InsertQuery q = QueryFactory.insert().into(databaseName() + "." + rejectsTableName())
                .value("Line", lastError.getLineNumber())
                .value("Content", lastError.getText());
        polyjdbc.simpleQueryRunner().insert(q);
        return true;
    }

    public String getInsertIntoStagingQuery(final List<CSVDataHeader> headers) {
        final List<String> colNames = new ArrayList<>(headers.size());
        final List<String> colValuesPlaceholder = new ArrayList<>(headers.size());
        for (CSVDataHeader header : headers) {
            colNames.add(header.name());
            colValuesPlaceholder.add(":" + header.name());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(databaseName()).append(".").append(dataSetTableName()).append("(");
        sb.append(Joiner.on(',').join(colNames));
        sb.append(") VALUES (");
        sb.append(Joiner.on(',').join(colValuesPlaceholder));
        sb.append(")");
        return sb.toString();
    }

    public int[] insertIntoStagingTableInBulk(final String sql, final List<CSVDataHeader> headers, final List<CSVLine> lines) {
        Sql2o sql2o = new Sql2o(EbeanServerManager.getManager().dataSource());

        final StatementRunnableWithResult statement = new StatementRunnableWithResult() {
            @Override
            public int[] run(Connection connection, Object argument) throws Throwable {
                Query query = connection.createQuery(sql);
                for (CSVLine line : lines) {
                    String[] values = line.getValues();
                    if (headers.size() != values.length) {
                        throw new RuntimeException("Lists should have the same number of elements");
                    }
                    int i = 0;
                    for (CSVDataHeader header : headers) {
                        String value = header.dbParsedValue(values[i]);

                        query.addParameter(header.name(), value);
                        i++;
                    }
                    query.addToBatch();
                }
                return query.executeBatch().getBatchResult();
            }
        };

        try {
            return sql2o.runInTransaction(statement);
        } catch (Throwable e) {
            while (e != null && !(e instanceof BatchUpdateException)) {
                e = e.getCause();
            }
            if (e instanceof BatchUpdateException) {
                BatchUpdateException bue = (BatchUpdateException) e;
                return bue.getUpdateCounts();
            } else {
               throw new RuntimeException(e);
            }
        }
    }


    private boolean createDataSetTable(final List<CSVDataHeader> headers) {
        Dialect dialect = DialectRegistry.MYSQL.getDialect();
        PolyJDBC polyjdbc = new PolyJDBC(EbeanServerManager.getManager().dataSource(), dialect);

        final String tableName = databaseName() + "." + dataSetTableName();
        SchemaManager schemaManager = null;
        try {
            schemaManager = polyjdbc.schemaManager();

            Schema schema = new Schema(dialect);
            RelationBuilder builder = schema.addRelation(tableName);
            for (CSVDataHeader cat : headers) {
                builder = builder.withAttribute().string(cat.name()).withMaxLength(256).and();
            }
            builder.build();
            schemaManager.create(schema);
            return true;
        } finally {
            polyjdbc.close(schemaManager);
        }
    }

    private boolean createRejectsTable() {
        Dialect dialect = DialectRegistry.MYSQL.getDialect();
        PolyJDBC polyjdbc = new PolyJDBC(EbeanServerManager.getManager().dataSource(), dialect);

        final String tableName = databaseName() + "." + rejectsTableName();
        SchemaManager schemaManager = null;
        try {
            schemaManager = polyjdbc.schemaManager();

            Schema schema = new Schema(dialect);
            schema.addRelation(tableName)
                    .withAttribute().longAttr("Line").and()
                    .withAttribute().string("Content").withMaxLength(4096).and()
                    .build();
            schemaManager.create(schema);
            return true;
        } finally {
            polyjdbc.close(schemaManager);
        }
    }

}
