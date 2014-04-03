package com.neutrino.models.metadata;

import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="QSPassBlockDetails")
public class QSPassBlockDetails extends Model {
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
    @Column(name="AttributeID")
    public Integer attributeID;

    public static Finder<QSPassDetailsKey, QSPassBlockDetails> find(final String serverName) {
        return new Finder<QSPassDetailsKey, QSPassBlockDetails>(
                serverName, QSPassDetailsKey.class, QSPassBlockDetails.class);
    }

}
