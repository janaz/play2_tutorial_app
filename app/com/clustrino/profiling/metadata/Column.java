package com.clustrino.profiling.metadata;

import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="columns")
public class Column extends Model {
    @Id
    public Long id;

    public String name;

    public String dataType;

    public Integer length;

    @ManyToOne
    @JoinColumn(name="data_set_id")
    public DataSet data_set;

    @NotNull
    public Long createdAt;

    public Long updatedAt;


}
