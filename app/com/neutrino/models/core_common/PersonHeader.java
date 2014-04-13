package com.neutrino.models.core_common;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name="PersonHeader")
public class PersonHeader extends Model {
    @Id
    @Column(name="HeaderID")
    public Integer id;

    @NotNull
    @Column(name="DataSetID")
    public Integer datasetId;

    @NotNull
    @SelectableAttribute
    @Column(name="SourceID", length = 60)
    public String sourceId;

    @NotNull
    @Column(name="CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name="MatchType", length = 3)
    public String matchType;

    @Column(name="MatchWeight", precision = 5, scale=2)
    public BigDecimal matchWeight;

    @Column(name="MatchSetID")
    public Integer matchSetID;

    public static Finder<Integer, PersonHeader> find(final String serverName) {
        return new Finder<Integer, PersonHeader>(
                serverName, Integer.class, PersonHeader.class);
    }


}
