package com.neutrino.models.core;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "PersonAnniversaryType")
public class PersonAnniversaryType extends Model implements CoreType {
    @Id
    @Column(name = "AnniversaryTypeID")
    public Integer id;

    @NotNull
    @Column(name = "Type", length = 30, unique = true)
    public String type;

    public static Finder<Integer, PersonAnniversaryType> find(final String serverName) {
        return new Finder<Integer, PersonAnniversaryType>(
                serverName, Integer.class, PersonAnniversaryType.class);
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
