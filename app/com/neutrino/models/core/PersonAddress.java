package com.neutrino.models.core;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "PersonAddress")
public class PersonAddress extends Model {
    @Id
    @Column(name = "AddressID")
    public Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "HeaderID")
    public PersonHeader header;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "AddressTypeID")
    public PersonAddressType addressType;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "HouseNumber", length = 10)
    public String houseNumber;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "HouseNumberSuffix", length = 3)
    public String houseNumberSuffix;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "StreetName", length = 30)
    public String streetName;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "StreetType", length = 5)
    public String streetType;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "StreetSuffix", length = 3)
    public String streetSuffix;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "PostalDelType", length = 12)
    public String postalDelType;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "PostalDelNumber", length = 10)
    public String postalDelNumber;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "PostalDelNumberSuffix", length = 3)
    public String postalDelNumberSuffix;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "FloorType", length = 3)
    public String floorType;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "FloorNumber", length = 10)
    public String floorNumber;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "UnitType", length = 5)
    public String unitType;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "UnitNumber", length = 10)
    public String unitNumber;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "BuildingName", length = 30)
    public String buildingName;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "Suburb", length = 60)
    public String suburb;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "Postcode", length = 10)
    public String postcode;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "State", length = 30)
    public String state;

    @SelectableAttribute(type=PersonAddressType.class)
    @Column(name = "Country", length = 60)
    public String country;

    @Column(name = "FullArea", length = 255)
    public String fullArea;

    @Column(name = "FullAddressLine", length = 255)
    public String fullAddressLine;

    @NotNull
    @Column(name = "CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name = "ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static Finder<Integer, PersonAddress> find(final String serverName) {
        return new Finder<Integer, PersonAddress>(
                serverName, Integer.class, PersonAddress.class);
    }

    public PersonAddressType getAddressType() {
        return addressType;
    }

    public PersonHeader getHeader() {
        return header;
    }

}
