package com.neutrino.models.core_common;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "PersonPhoneType")
public class PersonPhoneType extends Model implements CoreType {
    @Id
    @Column(name = "PhoneTypeID")
    public Integer id;

    @NotNull
    @Column(name = "Type", length = 30, unique = true)
    public String type;

    public static Finder<Integer, PersonPhoneType> find(final String serverName) {
        return new Finder<Integer, PersonPhoneType>(
                serverName, Integer.class, PersonPhoneType.class);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

}
