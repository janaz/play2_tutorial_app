package com.neutrino.models.metadata;

import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name="QSPassBlockDetails")
public class QSPassBlockDetails extends Model {
    @EmbeddedId
    public QSPassBlockDetailsKey id;

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

    public static Finder<QSPassBlockDetailsKey, QSPassBlockDetails> find(final String serverName) {
        return new Finder<QSPassBlockDetailsKey, QSPassBlockDetails>(
                serverName, QSPassBlockDetailsKey.class, QSPassBlockDetails.class);
    }

}
