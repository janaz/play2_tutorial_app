package com.clustrino.profiling.metadata;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name="data_sets")
public class DataSet extends Model {
    @Id
    public Long id;

    @NotNull
    public Long userId;

    public String type;

    @OneToMany(mappedBy="data_set")
    public List<Column> columns;

    @NotNull
    public Long createdAt;

    public Long updatedAt;
}
