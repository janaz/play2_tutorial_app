package com.neutrino.models.precore;

import com.neutrino.models.core_common.PersonHeader;
import com.neutrino.models.core_common.PersonNameType;
import com.neutrino.models.core_common.SelectableAttribute;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name="PersonName")
public class PersonName extends Model {
    @Id
    @Column(name="NameID")
    public Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name="HeaderID")
    public PersonHeader header;

    @NotNull
    @ManyToOne
    @JoinColumn(name="NameTypeID")
    public PersonNameType nameType;

    @Column(name="Salutation", length = 30)
    public String salutation;

    @Column(name="FirstName", length = 30)
    public String firstName;

    @Column(name="MiddleName", length = 30)
    public String middleName;

    @Column(name="Surname", length = 60)
    public String surname;

    @Column(name="Generation", length = 30)
    public String generation;

    @Column(name="Suffix", length = 30)
    public String suffix;

    @Column(name="FullName", length = 120)
    public String fullName;

    @NotNull
    @Column(name="CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name="ModificationTimestamp")
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

}
