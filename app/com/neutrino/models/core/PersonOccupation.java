package com.neutrino.models.core;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "PersonOccupation")
public class PersonOccupation extends Model {
    @Id
    @Column(name = "OccupationID")
    public Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "HeaderID")
    public PersonHeader header;

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

}
