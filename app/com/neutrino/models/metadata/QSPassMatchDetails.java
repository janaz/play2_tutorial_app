package com.neutrino.models.metadata;

import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="QSPassMatchDetails")
public class QSPassMatchDetails extends Model {
    @EmbeddedId
    public QSPassDetailsKey id;

    @ManyToOne
    @JoinColumns( {
            @JoinColumn(name = "DataSetID", referencedColumnName = "DataSetID"),
            @JoinColumn(name = "PassID", referencedColumnName = "ID") }
    )
    public QSPass qsPass;

    @NotNull
    @Column(name="ColumnName", length=64)
    public String columnName;

    @NotNull
    @Column(name="Category", length=30)
    public String category;

    @NotNull
    @Column(name="OpType", length=30)
    public String opType;

    @NotNull
    @Column(name="mProb")
    public Float mProb;

    @NotNull
    @Column(name="uProb")
    public Float uProb;

    @Column(name="Param1", length=30)
    public String param1;

    @Column(name="Param2", length=30)
    public String param2;

    public static Finder<QSPassDetailsKey, QSPassMatchDetails> find(final String serverName) {
        return new Finder<QSPassDetailsKey, QSPassMatchDetails>(
                serverName, QSPassDetailsKey.class, QSPassMatchDetails.class);
    }

}
