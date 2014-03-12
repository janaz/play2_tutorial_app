package com.neutrino.models.precore;

import com.neutrino.models.core_common.PersonAddressType;
import com.neutrino.models.core_common.PersonHeader;
import com.neutrino.models.core_common.SelectableAttribute;
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

    @Column(name = "HouseNumber", length = 10)
    public String houseNumber;

    @Column(name = "HouseNumberSuffix", length = 3)
    public String houseNumberSuffix;

    @Column(name = "StreetName", length = 30)
    public String streetName;

    @Column(name = "StreetType", length = 5)
    public String streetType;

    @Column(name = "StreetSuffix", length = 3)
    public String streetSuffix;

    @Column(name = "PostalDelType", length = 12)
    public String postalDelType;

    @Column(name = "PostalDelNumber", length = 10)
    public String postalDelNumber;

    @Column(name = "PostalDelNumberSuffix", length = 3)
    public String postalDelNumberSuffix;

    @Column(name = "FloorType", length = 3)
    public String floorType;

    @Column(name = "FloorNumber", length = 10)
    public String floorNumber;

    @Column(name = "UnitType", length = 5)
    public String unitType;

    @Column(name = "UnitNumber", length = 10)
    public String unitNumber;

    @Column(name = "BuildingName", length = 30)
    public String buildingName;

    @Column(name = "Suburb", length = 60)
    public String suburb;

    @Column(name = "Postcode", length = 10)
    public String postcode;

    @Column(name = "State", length = 30)
    public String state;

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
