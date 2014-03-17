package com.neutrino.data_loader;

import com.neutrino.models.core_common.PersonHeader;
import com.neutrino.models.core_common.ReferenceData;
import com.neutrino.models.core_common.WithCoreType;
import com.neutrino.models.metadata.ColumnMapping;
import com.neutrino.models.metadata.DataColumn;
import com.neutrino.models.metadata.DataSet;
import com.neutrino.profiling.*;
import play.db.ebean.Model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrecoreDataLoader {
    private final PrecoreSchema precoreSchema;
    private final int userId;
    private final int dataSetId;
    private final StagingSchema stagingSchema;
    private final MetadataSchema metadataSchema;

    public PrecoreDataLoader(int userId, int dataSetId) {
        this.dataSetId = dataSetId;
        this.userId = userId;
        this.precoreSchema = new PrecoreSchema(userId);
        this.stagingSchema = new StagingSchema(userId, dataSetId);
        this.metadataSchema = new MetadataSchema(userId);
    }


    private void blah() {
        DataSet ds = DataSet.find(metadataSchema.server().getName()).byId(dataSetId);
        List<ColumnMapping> mappings = ds.getMappings();
        final Map<String, Map<String, ColumnMapping>> mapmap= new HashMap<>();
        final Map<String, Map<String, DataColumn>> mapcol= new HashMap<>();
        for (ColumnMapping mapping : mappings) {
            DataColumn col = mapping.getDataColumn();
            col.getName(); // <-> mapping.coreTable/coreAttribute
            Map<String, ColumnMapping> mc = mapmap.get(mapping.coreTableName);
            Map<String, DataColumn> md = mapcol.get(mapping.coreTableName);
            if (mc == null) {
                mc = new HashMap<>();
                mapmap.put(mapping.coreTableName, mc);
            }
            if (md == null) {
                md = new HashMap<>();
                mapcol.put(mapping.coreTableName, md);
            }
            mc.put(mapping.coreAttributeName, mapping);
            md.put(mapping.coreAttributeName, col);
        }

        EbeanServerManager.getManager().executeQuery(stagingSchema.server(), new QueryCallable<Boolean>() {
            @Override
            public Boolean call(PreparedStatement pstmt) throws SQLException {
                ResultSet rs = pstmt.executeQuery();
                ResultSetMetaData met = rs.getMetaData();
                int columnCount = met.getColumnCount();
                Map<String, String> data = new HashMap<>();
                while (rs.next()) {
                    for (int i = 1; i < columnCount + 1; i++ ) {
                        String name = met.getColumnName(i);
                        data.put(name, rs.getString(i));
                    }
                    String srcName = mapcol.get("PersonHeader").get("SourceID").getName();
                    String srcVal = data.get(srcName);
                    PersonHeader ph = new PersonHeader();
                    ph.datasetId = dataSetId;
                    ph.creationTimestamp = new Date();
                    ph.sourceId = srcVal;
                    ph.save();
                    for (String tabName : mapcol.keySet()) {
                        if (!"PersonHeader".equals(tabName)) {
                            Model m = ReferenceData.instantiatePrecoreModelClass(tabName);
                            WithCoreType iface = (WithCoreType)m;
                            iface.setHeader(ph);
                            //create object through reflection
                            //populate person header
                            for (String attrName : mapcol.get(tabName).keySet()) {
                                String stgName = mapcol.get(tabName).get(attrName).getName();
                                String stgVal = data.get(stgName);
                                //pouplate attr with stgVal
                                //populate type

                                String type = mapmap.get(tabName).get(attrName).coreAttributeType;
                                iface.setTypeByName(type, precoreSchema.server().getName());

                                //get Type value from type class and search


                            }
                            //save record
                        }
                    }
                }
                return true;
            }

            @Override
            public String getQuery() {
                StringBuilder sb = new StringBuilder();
                return sb.append("SELECT *")
                        .append("FROM `").append(stagingSchema.databaseName()).append("`.`").append(stagingSchema.dataSetTableName()).append("`")
                        .toString();
            }

            @Override
            public void setup(PreparedStatement pstmt) throws SQLException {
            }
        });

    }

    private boolean populateSourceID() {
        return EbeanServerManager.getManager().executeQuery(precoreSchema.server(), new QueryCallable<Boolean>() {
            @Override
            public Boolean call(PreparedStatement pstmt) throws SQLException {
                return pstmt.execute();
            }

            @Override
            public String getQuery() {
                DataSet ds = DataSet.find(metadataSchema.server().getName()).byId(dataSetId);
                ColumnMapping cm = ColumnMapping.find(metadataSchema.server().getName()).where()
                        .eq("dataSet", ds)
                        .eq("coreTableName", "PersonHeader")
                        .eq("CoreAttributeName", "SourceID").findUnique();
                String stagingColumnName = cm.getDataColumn().getName();
                StringBuilder sb = new StringBuilder();
                return sb.append("INSERT INTO PersonHeader(DataSetID, SourceID, CreationTimestamp)")
                        .append("SELECT ?, `").append(stagingColumnName).append("`, NOW()")
                        .append("FROM `").append(stagingSchema.databaseName()).append("`.`").append(stagingSchema.dataSetTableName()).append("`")
                        .toString();
            }

            @Override
            public void setup(PreparedStatement pstmt) throws SQLException {
                pstmt.setInt(1, dataSetId);
            }
        });
    }

    public boolean testExecQry() {
        return EbeanServerManager.getManager().executeQuery(precoreSchema.server(), new QueryCallable<Boolean>() {
            @Override
            public Boolean call(PreparedStatement pstmt) throws SQLException {
                return pstmt.execute();
            }

            @Override
            public String getQuery() {
                StringBuilder sb = new StringBuilder();
                return sb.append("INSERT INTO blah(Line, Content) values(?,?)").toString();
            }

            @Override
            public void setup(PreparedStatement pstmt) throws SQLException {
                pstmt.setLong(1, 10);
                pstmt.setString(2, "blah");
            }
        });

    }

}
