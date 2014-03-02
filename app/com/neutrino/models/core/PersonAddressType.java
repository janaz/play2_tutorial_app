package com.neutrino.models.core;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "PersonAddressType")
public class PersonAddressType extends Model {
    @Id
    @Column(name = "AddressTypeID")
    public Integer id;

    @NotNull
    @Column(name = "Type", length = 30, unique = true)
    public String type;

    public static Finder<Integer, PersonAddressType> find(final String serverName) {
        return new Finder<Integer, PersonAddressType>(
                serverName, Integer.class, PersonAddressType.class);
    }


}
