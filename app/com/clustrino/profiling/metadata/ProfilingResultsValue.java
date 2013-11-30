package com.clustrino.profiling.metadata;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

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


}
