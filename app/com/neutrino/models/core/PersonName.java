package com.neutrino.models.core;

import com.neutrino.models.core_common.*;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "PersonName")
public class PersonName extends Model implements CoreTable {
    @Id
    @Column(name = "NameID")
    public Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "HeaderID")
    public PersonHeader header;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "NameTypeID")
    public PersonNameType nameType;

    @SelectableAttribute(type = PersonNameType.class)
    @Column(name = "Salutation", length = 30)
    public String salutation;

    @SelectableAttribute(type = PersonNameType.class)
    @Column(name = "FirstName", length = 30)
    public String firstName;

    @SelectableAttribute(type = PersonNameType.class)
    @Column(name = "MiddleName", length = 30)
    public String middleName;

    @SelectableAttribute(type = PersonNameType.class)
    @Column(name = "Surname", length = 60)
    public String surname;

    @SelectableAttribute(type = PersonNameType.class)
    @Column(name = "Generation", length = 30)
    public String generation;

    @SelectableAttribute(type = PersonNameType.class)
    @Column(name = "Suffix", length = 30)
    public String suffix;

    @NotNull
    @Column(name = "CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name = "ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static Finder<Integer, PersonName> find(final String serverName) {
        return new Finder<Integer, PersonName>(
                serverName, Integer.class, PersonName.class);
    }


    public PersonNameType getNameType() {
        return nameType;
    }

    public PersonHeader getHeader() {
        return header;
    }

    public void setNameType(PersonNameType nameType) {
        this.nameType = nameType;
    }

    @Override
    public void setTypeByName(String typeName, String serverName) {
        this.setNameType(PersonNameType.find(serverName).where().eq("name", typeName).findUnique());
    }

    @Override
    public Class<? extends CoreType> getCoreTypeClass() {
        return PersonNameType.class;
    }

    @Override
    public void setHeader(PersonHeader header) {
        this.header = header;
    }

}
