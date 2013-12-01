package com.clustrino.profiling.metadata;

import com.clustrino.profiling.MetadataSchema;
import com.clustrino.profiling.StagingSchema;
import models.configuration.ProfilingTemplate;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name="ProfilingResultsValue")
public class ProfilingResultsValue extends Model {
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

    public DataColumn getDataColumn() {
        return dataColumn;
    }

    public void setDataColumn(DataColumn dataColumn) {
        this.dataColumn = dataColumn;
    }


    public static void addResult(MetadataSchema mtd, StagingSchema stg, ProfilingTemplate template, DataColumn col, Map<String, String> results) {
        ProfilingResultsValue res = new ProfilingResultsValue();
        col.getResultsValues().add(res);
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
        col.save(mtd.server().getName());
    }
}
