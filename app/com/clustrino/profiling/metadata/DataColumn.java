package com.clustrino.profiling.metadata;

import com.clustrino.csv.DataCategory;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="DataColumn")
public class DataColumn extends Model {
    @Id
    @Column(name="ID")
    public Integer id;

    @Column(name="ColumnName", length = 64)
    public String name;

    @Column(name="ColumnDataType", length = 64)
    @Enumerated(EnumType.STRING)
    public DataCategory dataType;

    @Column(name="ColumnLength", length = 10)
    public String length;

    @ManyToOne
    @JoinColumn(name="DataSetID")
    public DataSet dataSet;

    @OneToMany(mappedBy="dataColumn", cascade = CascadeType.ALL)
    public List<ProfilingResultsColumn> resultsColumns;

    @OneToMany(mappedBy="dataColumn", cascade = CascadeType.ALL)
    public List<ProfilingResultsValue> resultsValues;

    @OneToMany(mappedBy="dataColumn", cascade = CascadeType.ALL)
    public List<ProfilingResultsFormat> resultsFormats;

    @Column(name="CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name="ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public List<ProfilingResultsColumn> getResultsColumns() {
        return resultsColumns;
    }

    public void setResultsColumns(List<ProfilingResultsColumn> resultsColumns) {
        this.resultsColumns = resultsColumns;
    }

    public List<ProfilingResultsValue> getResultsValues() {
        return resultsValues;
    }

    public void setResultsValues(List<ProfilingResultsValue> resultsValues) {
        this.resultsValues = resultsValues;
    }

    public List<ProfilingResultsFormat> getResultsFormats() {
        return resultsFormats;
    }

    public void setResultsFormats(List<ProfilingResultsFormat> resultsFormats) {
        this.resultsFormats = resultsFormats;
    }

}
