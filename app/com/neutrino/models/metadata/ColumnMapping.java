package com.neutrino.models.metadata;

import play.db.ebean.Model;

import javax.persistence.*;

@Entity
@Table(name="ColumnMapping")
public class ColumnMapping extends Model {
    @Id
    @Column(name="ID")
    public Integer id;

    @ManyToOne
    @JoinColumn(name="DataSetID")
    public DataSet dataSet;

    @ManyToOne
    @JoinColumn(name="DataColumnID")
    public DataColumn dataColumn;

    @Column(name="CoreTableName", length=64)
    public String coreTableName;

    @Column(name="CoreAttributeName", length=64)
    public String coreAttributeName;

    @Column(name="CoreAttributeType", length=30)
    public String coreAttributeType;

    @Column(name="Score")
    public Integer score;

    @Column(name="ConfidenceFlag")
    public Boolean confidenceFlag;

    @Column(name="MaybeFlag")
    public Boolean maybeFlag;

    @Column(name="ManualOverrideFlag")
    public Boolean manualOverrideFlag;

    public static Finder<Integer, ColumnMapping> find(final String serverName) {
        return new Finder<Integer, ColumnMapping>(
                serverName, Integer.class, ColumnMapping.class);
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }


}
