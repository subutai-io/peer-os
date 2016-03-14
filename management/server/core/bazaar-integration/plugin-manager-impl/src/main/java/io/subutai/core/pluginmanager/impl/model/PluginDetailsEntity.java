package io.subutai.core.pluginmanager.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.pluginmanager.api.model.PluginDetails;


@Entity
@Table( name = "plugin_details" )
@Access( AccessType.FIELD )
public class PluginDetailsEntity implements PluginDetails
{
    @Id
    @Column( name = "id" )
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;

    @Column( name = "name" )
    private String name;

    @Column( name = "version" )
    private String version;

    @Column( name = "kar" )
    private String kar;


    public Long getId()
    {
        return id;
    }


    public void setId( final Long id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getKar()
    {
        return kar;
    }


    public void setKar( final String kar )
    {
        this.kar = kar;
    }


    public String getVersion()
    {
        return version;
    }


    public void setVersion( final String version )
    {
        this.version = version;
    }
}
