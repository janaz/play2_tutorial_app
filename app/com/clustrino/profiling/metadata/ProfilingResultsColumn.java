package com.clustrino.profiling.metadata;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="ProfilingResultsColumn")
public class ProfilingResultsColumn extends Model {
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

    @NotNull
    @Column(name="TotalCount")
    public Integer totalCount;

    @NotNull
    @Column(name="DistinctCount")
    public Integer distinctCount;

    @NotNull
    @Column(name="NullCount")
    public Integer nullCount;

    @NotNull
    @Column(name="PercentagePopulated")
    public BigDecimal percentagePopulated;

    @NotNull
    @Column(name="PercentageUnique")
    public BigDecimal percentageUnique;

    @NotNull
    @Column(name="MinimumLength")
    public Integer minimumLength;

    @NotNull
    @Column(name="MaximumLength")
    public Integer maximumLength;

    @Column(name="MinimumValue", length = 512)
    public String minimumValue;

    @Column(name="MaximumValue", length = 512)
    public String maximumValue;

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
