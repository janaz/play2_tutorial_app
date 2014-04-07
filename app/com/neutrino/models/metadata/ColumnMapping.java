package com.neutrino.models.metadata;

import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Entity
@Table(name="ColumnMapping")
public class ColumnMapping extends Model {
    @Id
    @Column(name="ID")
    public Integer id;

    @ManyToOne
    @NotNull
    @JoinColumn(name="DataSetID")
    public DataSet dataSet;

    @OneToOne
    @JoinColumn(name="DataColumnID")
    public DataColumn dataColumn;

    @Column(name="MixedContentFlag")
    public Boolean mixedContentFlag = false;

    @Column(name="CoreTableName", length=64)
    public String coreTableName;

    @Column(name="CoreAttributeName", length=64)
    public String coreAttributeName;

    @Column(name="CoreAttributeType", length=30)
    public String coreAttributeType;

    @Column(name="MandatoryFlag")
    public Boolean mandatoryFlag = false;

    @Column(name="Score")
    public Integer score;

    @Column(name="ConfidenceFlag")
    public Boolean confidenceFlag = false;

    @Column(name="MaybeFlag")
    public Boolean maybeFlag = false;

    @Column(name="ManualOverrideFlag")
    public Boolean manualOverrideFlag = false;

    public static Finder<Integer, ColumnMapping> find(final String serverName) {
        return new Finder<Integer, ColumnMapping>(
                serverName, Integer.class, ColumnMapping.class);
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public DataColumn getDataColumn() {
        return dataColumn;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void setDataColumn(DataColumn dataColumn) {
        this.dataColumn = dataColumn;
    }


    public String getConfidence() {
        if (confidenceFlag) {
            return "Confident ("+score+")";
        }else if (maybeFlag) {
            return "Maybe ("+score+")";
        } else {
            return "Gave up ("+score+")";
        }
    }

    private boolean areEqual(Object o1, Object o2) {
        if (o1 == null) {
            return (o2==null);
        } else if (o2 ==null) {
            return false;
        } else {
            return o1.equals(o2);
        }
    }
    public void updateFromJson(Map<String, String> jsonMapping, String serverName) {
        String newTable = jsonMapping.get("table_name");
        String newAttr = jsonMapping.get("attribute_name");
        String newType = jsonMapping.get("attribute_type");
        if (newTable.isEmpty()) {
            newTable = null;
            newAttr = null;
            newType = null;
        }
        if (!areEqual(newTable, coreTableName) || !areEqual(newAttr, coreAttributeName)) {
            //we have an override
            setCoreTableName(newTable);
            setCoreAttributeName(newAttr);
            setCoreAttributeType(newType);
            setManualOverrideFlag(true);
        } else {
            //change of type
            setCoreAttributeType(newType);
        }
        save(serverName);
    }

    public void setCoreTableName(String coreTableName) {
        this.coreTableName = coreTableName;
    }

    public void setCoreAttributeName(String coreAttributeName) {
        this.coreAttributeName = coreAttributeName;
    }

    public void setCoreAttributeType(String coreAttributeType) {
        this.coreAttributeType = coreAttributeType;
    }

    public void setManualOverrideFlag(Boolean manualOverrideFlag) {
        this.manualOverrideFlag = manualOverrideFlag;
    }


    public String getCoreTableName() {
        return coreTableName;
    }

    public String getCoreAttributeName() {
        return coreAttributeName;
    }

    public String getCoreAttributeType() {
        return coreAttributeType;
    }
}
