package io.subutai.core.identity.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.identity.api.model.Permission;


/**
 *
 */
@Entity
@Table( name = "permission" )
@Access( AccessType.FIELD )
public class PermissionEntity implements Permission
{
    @Id
    @GeneratedValue
    @Column( name = "id" )
    private Long id;

    @Column( name = "name" )
    private String name;

    @Column( name = "type" )
    private Short type = 1;


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


    public Short getType()
    {
        return type;
    }


    public void setType( final Short type )
    {
        this.type = type;
    }
}
