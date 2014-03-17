package com.neutrino.models.core_common;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "PersonGender")
public class PersonGender extends Model implements CoreTable {
    @Id
    @Column(name = "GenderID")
    public Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "HeaderID")
    public PersonHeader header;

    @SelectableAttribute
    @Column(name = "GenderCode", length = 20)
    public String genderCode;

    @NotNull
    @Column(name = "CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name = "ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static Finder<Integer, PersonGender> find(final String serverName) {
        return new Finder<Integer, PersonGender>(
                serverName, Integer.class, PersonGender.class);
    }

    public PersonHeader getHeader() {
        return header;
    }

    @Override
    public Class<? extends CoreType> getCoreTypeClass() {
        return EmptyType.class;
    }

    @Override
    public void setTypeByName(String typeName, String serverName) {

    }

    @Override
    public void setHeader(PersonHeader header) {
        this.header = header;
    }
}
