package com.neutrino.models.core;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name="PersonPhone")
public class PersonPhone extends Model {
    @Id
    @Column(name="PhoneID")
    public Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name="HeaderID")
    public PersonHeader header;

    @NotNull
    @ManyToOne
    @JoinColumn(name="PhoneTypeID")
    public PersonPhoneType phoneType;

    @Column(name="CountryCode")
    public Short countryCode;

    @Column(name="AreaCode")
    public Short areaCode;

    @Column(name="PhoneNumber")
    public Long phoneNumber;

    @Column(name="Extension")
    public Integer extension;

    @NotNull
    @Column(name="CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name="ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static Finder<Integer, PersonPhone> find(final String serverName) {
        return new Finder<Integer, PersonPhone>(
                serverName, Integer.class, PersonPhone.class);
    }


    public PersonPhoneType getPhoneType() {
        return phoneType;
    }

    public PersonHeader getHeader() {
        return header;
    }

}
