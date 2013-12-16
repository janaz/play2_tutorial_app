package com.neutrino.models.configuration;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "DataFormat")
public class DataFormat extends Model {

	@Id
    @Column(name="ID")
    public Integer id;

    @Column(name="Code", length = 30)
    public String code;

    @Column(name="Format", length = 60)
    public String format;

    @Column(name="CreationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date creationTimestamp;

    @Column(name="ModificationTimestamp")
    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date modificationTimestamp;

    public static final Finder<Integer, DataFormat> find = new Finder<Integer, DataFormat>(
            Integer.class, DataFormat.class);

}
