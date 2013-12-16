package com.neutrino.models.configuration;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "ReferenceData")
public class ReferenceData extends Model {

	@Id
    @Column(name="ID")
    public Integer id;

    @Column(name="Code", length = 30)
    public String code;

    @Column(name="Value", length = 120)
    public String value;

    @Column(name="CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name="ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static final Finder<Integer, ReferenceData> find = new Finder<Integer, ReferenceData>(
            Integer.class, ReferenceData.class);

}
