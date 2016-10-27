package io.subutai.core.bazaar.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.bazaar.api.model.Plugin;


@Entity
@Table( name = "hub_plugin" )
@Access( AccessType.FIELD )
public class PluginEntity implements Plugin
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

    @Column( name = "url" )
    private String url;

    @Column( name = "uid" )
    private String uid;


    @Override
    public Long getId()
    {
        return id;
    }


    @Override
    public void setId( final Long id )
    {
        this.id = id;
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public void setName( final String name )
    {
        this.name = name;
    }


    @Override
    public String getKar()
    {
        return kar;
    }


    @Override
    public void setKar( final String kar )
    {
        this.kar = kar;
    }


    @Override
    public String getVersion()
    {
        return version;
    }


    @Override
    public void setVersion( final String version )
    {
        this.version = version;
    }


    @Override
    public String getUrl()
    {
        return this.url;
    }


    @Override
    public void setUrl( String url )
    {
        this.url = url;
    }


    @Override
    public String getUid()
    {
        return uid;
    }


    @Override
    public void setUid( String uid )
    {
        this.uid = uid;
    }
}
