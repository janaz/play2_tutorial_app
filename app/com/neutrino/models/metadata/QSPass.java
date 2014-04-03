package com.neutrino.models.metadata;

import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name="QSPass")
public class QSPass extends Model {
    @EmbeddedId
    public QSPassKey id;

    @ManyToOne
    @NotNull
    @JoinColumn(name="DataSetID")
    public DataSet dataSet;

    @NotNull
    @Column(name="PassName", length=64, unique = true)
    public String passName;

    @NotNull
    @Column(name = "MatchCuttOff", precision = 10, scale = 2)
    public BigDecimal matchCuttOff;

    @NotNull
    @Column(name = "ClericalCuttOff", precision = 10, scale = 2)
    public BigDecimal clericalCuttOff;

    @Column(name="Overflow")
    public Integer overflow;

    public static Finder<QSPassKey, QSPass> find(final String serverName) {
        return new Finder<QSPassKey, QSPass>(
                serverName, QSPassKey.class, QSPass.class);
    }

    public DataSet getDataSet() {
        return dataSet;
    }

}
