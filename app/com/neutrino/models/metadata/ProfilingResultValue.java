package com.neutrino.models.metadata;

import com.neutrino.profiling.MetadataSchema;
import com.neutrino.profiling.StagingSchema;
import com.neutrino.models.configuration.ProfilingTemplate;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name="ProfilingResultValue")
public class ProfilingResultValue extends Model {
    @Id
    @Column(name="ID")
    public Integer id;

    @NotNull
    @Column(name="ProfilingTemplateID")
    public Integer profilingTemplateId;

    @Column(name="TableName", length = 64)
    public String tableName;

    @Column(name="ColumnName", length = 64)
    public String columnName;

    @Column(name="Value", length = 128)
    public String value;

    @NotNull
    @Column(name="Cardinality")
    public Integer cardinality;

    @ManyToOne
    @NotNull
    @JoinColumn(name="DataColumnID")
    public DataColumn dataColumn;

    @Column(name="CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    public static Finder<Integer, ProfilingResultValue> find(final String serverName) {
        return new Finder<Integer, ProfilingResultValue>(
                serverName, Integer.class, ProfilingResultValue.class);
    }

    public DataColumn getDataColumn() {
        return dataColumn;
    }

    public void setDataColumn(DataColumn dataColumn) {
        this.dataColumn = dataColumn;
    }

    public static void addResult(MetadataSchema mtd, StagingSchema stg, ProfilingTemplate template, DataColumn col, Map<String, String> results) {
        ProfilingResultValue res = new ProfilingResultValue();
        try {
            res.cardinality = Integer.valueOf(results.get("Cardinality"));
        }catch (NumberFormatException e) {
            res.cardinality = 0;
        }
        res.value = results.get("Value");
        res.columnName = col.name;
        res.profilingTemplateId = template.id;
        res.tableName = stg.dataSetTableName();
        res.setDataColumn(col);
        res.save(mtd.server().getName());
    }
}
