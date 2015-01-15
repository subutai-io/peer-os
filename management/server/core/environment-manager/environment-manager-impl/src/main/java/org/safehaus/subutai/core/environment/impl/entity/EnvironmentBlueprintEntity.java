package org.safehaus.subutai.core.environment.impl.entity;


import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;


/**
 * Created by nisakov on 1/12/15.
 */
@Entity
@Table( name = "environment_blueprint" )
@Access( AccessType.FIELD )

public class EnvironmentBlueprintEntity
{
    @Id
    @Column( name = "id" )
    private String id;

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


    public String getInfo()
    {
        return info;
    }


    public void setInfo( final String info )
    {
        this.info = info;
    }
}
