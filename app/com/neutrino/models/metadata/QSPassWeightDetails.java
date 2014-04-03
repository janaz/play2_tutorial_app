package com.neutrino.models.metadata;

import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name="QSPassWeightDetails")
public class QSPassWeightDetails extends Model {
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

    @Column(name="AR", length=5)
    public String ar;

    @NotNull
    @Column(name="AV", length=30)
    public String av;

    @NotNull
    @Column(name="BV", length=30)
    public String bv;

    @Column(name = "AW", precision = 10, scale = 2)
    public BigDecimal aw;

    @Column(name = "DW", precision = 10, scale = 2)
    public BigDecimal dw;

    @Column(name = "AM", precision = 10, scale = 2)
    public BigDecimal am;

    @Column(name = "BM", precision = 10, scale = 2)
    public BigDecimal bm;

    @Column(name = "XM", precision = 10, scale = 2)
    public BigDecimal xm;

    public static Finder<QSPassDetailsKey, QSPassWeightDetails> find(final String serverName) {
        return new Finder<QSPassDetailsKey, QSPassWeightDetails>(
                serverName, QSPassDetailsKey.class, QSPassWeightDetails.class);
    }

}
