package com.neutrino.models.core;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "PersonExternalIdentifier")
public class PersonExternalIdentifier extends Model {
    @Id
    @Column(name = "ExternalIdentifierID")
    public Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "HeaderID")
    public PersonHeader header;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "ExternalIdentifierTypeID")
    public PersonExternalIdentifierType externalIdentifierType;

    @SelectableAttribute(type=PersonExternalIdentifierType.class)
    @Column(name = "Identifier", length = 30)
    public String Identifier;

    @SelectableAttribute(type=PersonExternalIdentifierType.class)
    @Column(name = "ExpiryDate")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date expiryDate;

    @NotNull
    @Column(name = "CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name = "ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static Finder<Integer, PersonExternalIdentifier> find(final String serverName) {
        return new Finder<Integer, PersonExternalIdentifier>(
                serverName, Integer.class, PersonExternalIdentifier.class);
    }


    public PersonExternalIdentifierType getExternalIdentifierType() {
        return externalIdentifierType;
    }

    public PersonHeader getHeader() {
        return header;
    }

}
