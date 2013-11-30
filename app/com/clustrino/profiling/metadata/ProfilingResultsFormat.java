package com.clustrino.profiling.metadata;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name="ProfilingResultsFormat")
public class ProfilingResultsFormat extends Model {
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

    @Column(name="Format", length = 128)
    public String format;

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


}
