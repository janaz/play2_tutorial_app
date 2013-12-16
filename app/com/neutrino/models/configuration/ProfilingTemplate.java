package com.neutrino.models.configuration;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "ProfilingTemplate")
public class ProfilingTemplate extends Model {

	@Id
    @Column(name="ID")
    public Integer id;

    @Column(name="ExecutionLevel", length = 10)
    public String executionLevel;

    @Column(name="TargetTableName", length = 64, unique = true)
    public String targetTableName;

    @Column(name="Description", length = 500)
    public String description;

    @Column(name="TemplateQuery", length = 2000)
    public String templateQuery;

	public static final Finder<Integer, ProfilingTemplate> find = new Finder<Integer, ProfilingTemplate>(
            Integer.class, ProfilingTemplate.class);

}
