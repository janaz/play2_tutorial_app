package com.clustrino.profiling.metadata;

import com.avaje.ebean.annotation.EnumValue;
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
        @EnumValue("PARSING")
        PARSING,

        @EnumValue("PARSED")
        PARSED,

        @EnumValue("ERROR")
        ERROR
    }

    @Id
    @Column(name="ID")
    public Integer id;

    @NotNull
    @Column(name="UserID")
    public Integer userId;

    @Column(name="State")
    public State state;

    @Column(name="Type")
    public Type type;

    @OneToMany(mappedBy="dataSet", cascade=CascadeType.ALL)
    public List<DataColumn> columns;

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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public List<DataColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<DataColumn> columns) {
        this.columns = columns;
    }


}
