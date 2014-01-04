package com.neutrino.models.metadata;

import play.db.ebean.Model;

import javax.persistence.*;

@Entity
@Table(name="CoreModelStructure")
public class CoreModelStructure extends Model {
    @Id
    @Column(name="ID")
    public Integer id;

    @ManyToOne
    @JoinColumn(name="ColumnMappingID")
    public ColumnMapping columnMapping;

    @Column(name="DataType", length=30)
    public String dataType;

    @Column(name="DataLength")
    public Integer dataLength;

    @Column(name="CoreModelStructurecol", length=45)
    public String coreModelStructurecol;

    public static Finder<Integer, CoreModelStructure> find(final String serverName) {
        return new Finder<Integer, CoreModelStructure>(
                serverName, Integer.class, CoreModelStructure.class);
    }

    public ColumnMapping getColumnMapping() {
        return columnMapping;
    }

    public void setColumnMapping(ColumnMapping columnMapping) {
        this.columnMapping = columnMapping;
    }


}
