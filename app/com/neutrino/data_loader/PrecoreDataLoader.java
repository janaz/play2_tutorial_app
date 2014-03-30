package com.neutrino.data_loader;

import com.neutrino.models.core_common.CoreTable;
import com.neutrino.models.core_common.PersonHeader;
import com.neutrino.models.core_common.ReferenceData;
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

    public void populate() {
        //populateSourceID();
        populatePrecore();
    }
    private void populatePrecore() {
        DataSet ds = DataSet.find(metadataSchema.server().getName()).byId(dataSetId);
        List<ColumnMapping> mappings = ds.getMappings();
        final Map<String, Map<String, ColumnMapping>> mapmap = new HashMap<>();
        for (ColumnMapping mapping : mappings) {
            Map<String, ColumnMapping> mc = mapmap.get(mapping.coreTableName);
            if (mc == null) {
                mc = new HashMap<>();
                mapmap.put(mapping.coreTableName, mc);
            }
            mc.put(mapping.coreAttributeName, mapping);
        }

        EbeanServerManager.getManager().executeQuery(null, new QueryCallable<Boolean>() {
            @Override
            public Boolean call(PreparedStatement pstmt) throws SQLException {
                ResultSet rs = pstmt.executeQuery();
                ResultSetMetaData met = rs.getMetaData();
                int columnCount = met.getColumnCount();
                Map<String, String> data = new HashMap<>();
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String name = met.getColumnName(i);
                        data.put(name, rs.getString(i));
                    }
                    String srcName = mapmap.get("PersonHeader").get("SourceID").getDataColumn().getName();
                    String srcVal = data.get(srcName);
                    PersonHeader ph = new PersonHeader();
                    ph.datasetId = dataSetId;
                    ph.creationTimestamp = new Date();
                    ph.sourceId = srcVal;
                    ph.save(precoreSchema.server().getName());
                    for (String tabName : mapmap.keySet()) {
                        if (!"PersonHeader".equals(tabName)) {
                            Map<String, Model> models = new HashMap<>();
                            for (String attrName : mapmap.get(tabName).keySet()) {
                                String type = mapmap.get(tabName).get(attrName).coreAttributeType;
                                if (type == null) {
                                    type = "NULL";
                                }

                                Model m = models.get(type);
                                if (m == null) {
                                    m = ReferenceData.instantiatePrecoreModelClass(tabName);
                                    models.put(type, m);
                                    CoreTable iface = (CoreTable) m;
                                    iface.setHeader(ph);
                                    iface.setTypeByName(type, precoreSchema.server().getName());
                                }

                                String stgName = mapmap.get(tabName).get(attrName).getDataColumn().getName();
                                String stgVal = data.get(stgName);
                                ReferenceData.setValue(m, attrName, stgVal);
                            }
                            for (String type : models.keySet()) {
                                models.get(type).save(precoreSchema.server().getName());
                            }
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


}
