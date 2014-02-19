package com.neutrino.models.metadata;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
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
    public String dataType;

    @Column(name="ColumnLength", length = 10)
    public String length;

    @ManyToOne
    @JoinColumn(name="DataSetID")
    public DataSet dataSet;

    @OneToMany(mappedBy="dataColumn", cascade = CascadeType.ALL)
    public List<ProfilingResultColumn> resultsColumns;

    @OneToMany(mappedBy="dataColumn", cascade = CascadeType.ALL)
    public List<ProfilingResultValue> resultsValues;

    @OneToMany(mappedBy="dataColumn", cascade = CascadeType.ALL)
    public List<ProfilingResultFormat> resultsFormats;

    @Column(name="CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name="ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static Finder<Integer, DataColumn> find(final String serverName) {
        return new Finder<Integer, DataColumn>(
                serverName, Integer.class, DataColumn.class);
    }


    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public List<ProfilingResultColumn> getResultsColumns() {
        return resultsColumns;
    }

    public void setResultsColumns(List<ProfilingResultColumn> resultsColumns) {
        this.resultsColumns = resultsColumns;
    }

    public List<ProfilingResultValue> getResultsValues() {
        return resultsValues;
    }

    public void setResultsValues(List<ProfilingResultValue> resultsValues) {
        this.resultsValues = resultsValues;
    }

    public List<ProfilingResultFormat> getResultsFormats() {
        return resultsFormats;
    }

    public void setResultsFormats(List<ProfilingResultFormat> resultsFormats) {
        this.resultsFormats = resultsFormats;
    }

}
