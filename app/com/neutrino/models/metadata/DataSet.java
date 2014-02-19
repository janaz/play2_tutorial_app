package com.neutrino.models.metadata;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="DataSet")
public class DataSet extends Model {
    public enum Type {
        @EnumValue("FILE")
        FILE,
        @EnumValue("TABLE")
        TABLE
    }

    public enum State {
        @EnumValue("LOADED")
        LOADED,
        @EnumValue("PROFILING")
        PROFILING,
        @EnumValue("PROFILING_DONE")
        PROFILING_DONE,
        @EnumValue("PROFILING_ERROR")
        PROFILING_ERROR,
        @EnumValue("AUTO_MAPPING")
        AUTO_MAPPING,
        @EnumValue("AUTO_MAPPING_DONE")
        AUTO_MAPPING_DONE,
        @EnumValue("AUTO_MAPPING_ERROR")
        AUTO_MAPPING_ERROR,
        @EnumValue("MANUAL_MAPPING_DONE")
        MANUAL_MAPPING_DONE
    }

    @Id
    @Column(name="ID")
    public Integer id;

    @NotNull
    @Column(name="UserID")
    public Integer userId;

    @Column(name="State", length=30)
    public State state;

    @Column(name="Type", length=30)
    public Type type;

    @OneToMany(mappedBy="dataSet", cascade=CascadeType.ALL)
    public List<DataColumn> columns;

    @OneToMany(mappedBy="dataSet", cascade=CascadeType.ALL)
    public List<ColumnMapping> mappings;

    @OneToOne(mappedBy="dataSet", cascade=CascadeType.ALL, optional = true)
    public File file;

    @NotNull
    @Column(name="CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name="ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static Finder<Integer, DataSet> find(final String serverName) {
        return new Finder<Integer, DataSet>(
                serverName, Integer.class, DataSet.class);
    }

    @JsonIgnore
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @JsonIgnore
    public List<DataColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<DataColumn> columns) {
        this.columns = columns;
    }

    @JsonIgnore
    public List<ColumnMapping> getMappings() {
        return mappings;
    }

}
