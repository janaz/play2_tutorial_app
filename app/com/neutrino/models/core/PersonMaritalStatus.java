package com.neutrino.models.core;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "PersonMaritalStatus")
public class PersonMaritalStatus extends Model {
    @Id
    @Column(name = "MaritalStatusID")
    public Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "HeaderID")
    public PersonHeader header;

    @Column(name = "MaritalStatusCode", length = 30)
    public String maritalStatusCode;

    @NotNull
    @Column(name = "CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name = "ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static Finder<Integer, PersonMaritalStatus> find(final String serverName) {
        return new Finder<Integer, PersonMaritalStatus>(
                serverName, Integer.class, PersonMaritalStatus.class);
    }

    public PersonHeader getHeader() {
        return header;
    }

}
