package com.neutrino.models.core;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "PersonAnniversary")
public class PersonAnniversary extends Model {
    @Id
    @Column(name = "AnniversaryID")
    public Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "HeaderID")
    public PersonHeader header;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "AnniversaryTypeID")
    public PersonAnniversaryType anniversaryType;

    @Column(name = "AnniversaryDate")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date anniversaryDate;

    @NotNull
    @Column(name = "CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name = "ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static Finder<Integer, PersonAnniversary> find(final String serverName) {
        return new Finder<Integer, PersonAnniversary>(
                serverName, Integer.class, PersonAnniversary.class);
    }

    public PersonAnniversaryType getAnniversaryType() {
        return anniversaryType;
    }

    public PersonHeader getHeader() {
        return header;
    }

}
