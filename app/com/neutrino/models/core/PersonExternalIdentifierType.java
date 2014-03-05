package com.neutrino.models.core;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "PersonExternalIdentifierType")
public class PersonExternalIdentifierType extends Model implements CoreType {
    @Id
    @Column(name = "ExternalIdentifierTypeID")
    public Integer id;

    @NotNull
    @Column(name = "Type", length = 30, unique = true)
    public String type;

    public static Finder<Integer, PersonExternalIdentifierType> find(final String serverName) {
        return new Finder<Integer, PersonExternalIdentifierType>(
                serverName, Integer.class, PersonExternalIdentifierType.class);
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
