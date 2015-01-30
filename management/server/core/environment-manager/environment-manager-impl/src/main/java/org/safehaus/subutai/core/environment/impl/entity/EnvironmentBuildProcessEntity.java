package org.safehaus.subutai.core.environment.impl.entity;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Id;



@Entity
@Table( name = "environment_build_process" )
@Access( AccessType.FIELD )
@IdClass(EnvironmentBuildProcessEntityPk.class)

public class EnvironmentBuildProcessEntity
{
    @Id
    @Column( name = "id" )
    private String id;

    @Id
    @Column( name = "source" )
    private String source;

    @Lob
    @Column( name = "info" )
    private String info;


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getSource()
    {
        return source;
    }


    public void setSource( final String source )
    {
        this.source = source;
    }


    public String getInfo()
    {
        return info;
    }


    public void setInfo( final String info )
    {
        this.info = info;
    }
}
