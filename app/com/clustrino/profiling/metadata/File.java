package com.clustrino.profiling.metadata;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="files")

public class File  extends Model {
    @Id
    public Long id;

    @NotNull
    public String originalFileName;

    public String fileLocation;

    public String fileFormat;

    public String delimiter;

    public String quote;

    public String headerFlag;

    @OneToOne
    public DataSet dataSet;

    @NotNull
    public Long createdAt;

    public Long updatedAt;

}
