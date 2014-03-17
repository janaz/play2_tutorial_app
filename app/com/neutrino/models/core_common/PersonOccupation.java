package com.neutrino.models.core_common;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "PersonOccupation")
public class PersonOccupation extends Model implements CoreTable {
    @Id
    @Column(name = "OccupationID")
    public Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "HeaderID")
    public PersonHeader header;

    @SelectableAttribute
    @Column(name = "OccupationCode", length = 30)
    public String occupationCode;

    @NotNull
    @Column(name = "CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name = "ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static Finder<Integer, PersonOccupation> find(final String serverName) {
        return new Finder<Integer, PersonOccupation>(
                serverName, Integer.class, PersonOccupation.class);
    }

    public PersonHeader getHeader() {
        return header;
    }

    @Override
    public Class<? extends CoreType> getCoreTypeClass() {
        return EmptyType.class;
    }

    @Override
    public void setTypeByName(String typeName, String serverName) {

    }

    @Override
    public void setHeader(PersonHeader header) {
        this.header = header;
    }
}
