package controllers.neutrino;

import com.jolbox.bonecp.BoneCPDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.neutrino.data_loader.RefData;
import com.neutrino.datamappingdiscovery.DataMapping;
import org.polyjdbc.core.PolyJDBC;
import org.polyjdbc.core.dialect.Dialect;
import org.polyjdbc.core.dialect.DialectRegistry;
import org.polyjdbc.core.schema.DDLQuery;
import org.polyjdbc.core.schema.SchemaManager;
import org.polyjdbc.core.schema.model.Schema;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.StatementRunnable;
import play.mvc.Controller;
import play.mvc.Result;

import java.beans.PropertyVetoException;
import java.sql.BatchUpdateException;
import java.util.Arrays;

public class BasicFlow extends Controller {


    private static void test(String tableName) {

        try {
            // load the database driver (make sure this is in your classpath!)
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


        // setup the connection pool
        BoneCPDataSource ds = new BoneCPDataSource();
        ds.setDriverClass("com.mysql.jdbc.Driver");
        ds.setJdbcUrl("jdbc:mysql://localhost:3306/mysql?useUnicode=yes&characterEncoding=UTF8&sessionVariables=storage_engine=InnoDB");
        ds.setUsername("root");
        ds.setPassword("");
        ds.setMinConnectionsPerPartition(5);
        ds.setMaxConnectionsPerPartition(10);
        ds.setPartitionCount(1);
        //BoneCP(config); // setup the connection pool


        ComboPooledDataSource cpds = new ComboPooledDataSource();
        try {
            cpds.setDriverClass("com.mysql.jdbc.Driver");
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }

        cpds.setJdbcUrl("jdbc:mysql://localhost:3306/mysql?useUnicode=yes&characterEncoding=UTF8&sessionVariables=storage_engine=InnoDB");
        cpds.setUser("root");
        cpds.setPassword("");
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
        cpds.setPreferredTestQuery("select 1");
        cpds.setIdleConnectionTestPeriod(60);
        Dialect dialect = DialectRegistry.MYSQL.getDialect();
        PolyJDBC polyjdbc = new PolyJDBC(cpds, dialect);
        SchemaManager schemaManager = null;
        try {
            schemaManager = polyjdbc.schemaManager();

            Schema schema = new Schema(dialect);
            schema.addRelation(tableName)
                    .withAttribute().longAttr("id").withAdditionalModifiers("AUTO_INCREMENT").notNull().and()
                    .withAttribute().string("name").withMaxLength(2).notNull().unique().and()
                    .withAttribute().integer("age").notNull().and()
                    .primaryKey("pk_test_one").using("id").and()
                    .build();

            schemaManager.create(schema);
            schemaManager.ddl(DDLQuery.ddl("ALTER TABLE " + tableName + " MODIFY COLUMN name VARCHAR(5);"));
        } finally {
            polyjdbc.close(schemaManager);
        }
        Sql2o sql2o = new Sql2o(cpds);
        final String sql = "INSERT INTO " + tableName + "(name, age) VALUES (:name, :age)";

        try {
            sql2o.runInTransaction(new StatementRunnable() {
                public void run(Connection connection, Object argument) throws Throwable {
                    Query query = connection.createQuery(sql);

                    for (int i = 0; i < 100; i++) {
                        query.addParameter("name", "name" + i).addParameter("age", i).addToBatch();
                    }
                    for (int i = 0; i < 100; i++) {
                        query.addParameter("name", "bame" + i).addParameter("age", i).addToBatch();
                    }

                    int[] res = query.executeBatch().getBatchResult();
                    System.out.println(Arrays.toString(res));
                }
            });
        } catch (Throwable e) {
            while (e != null && !(e instanceof BatchUpdateException)) {
                e = e.getCause();
            }
            if (e instanceof BatchUpdateException) {
                BatchUpdateException bue = (BatchUpdateException) e;
                System.out.println(Arrays.toString(bue.getUpdateCounts()));
            }
        }
    }

    private static void test2() {
        BoneCPDataSource ds = new BoneCPDataSource();
        ds.setDriverClass("com.mysql.jdbc.Driver");
        ds.setJdbcUrl("jdbc:mysql://localhost:3306/mysql?useUnicode=yes&characterEncoding=UTF8&sessionVariables=storage_engine=InnoDB");
        ds.setUsername("root");
        ds.setPassword("");
        ds.setMinConnectionsPerPartition(5);
        ds.setMaxConnectionsPerPartition(10);
        ds.setPartitionCount(1);

        RefData.CORE_SCHEMA.forUser(323).create(ds);
        RefData.CORE_SCHEMA.forUser(323).populate(ds);

    }
    public static Result index() {
        DataMapping.createPrecoreSchema(8);
        //test("Staging008.StgTest07");
        return ok(views.html.neutrino.index.render());
    }

    public static Result testAction() {
        return ok(views.html.neutrino.test_action.render());
    }


}
