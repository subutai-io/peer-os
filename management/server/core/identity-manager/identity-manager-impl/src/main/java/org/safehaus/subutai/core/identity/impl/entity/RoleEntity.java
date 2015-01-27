package org.safehaus.subutai.core.identity.impl.entity;


import java.util.Arrays;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.safehaus.subutai.core.identity.api.Role;


/**
 * Implementation of Role interface.
 */
@Entity
@Table( name = "role" )
@Access( AccessType.FIELD )
public class RoleEntity implements Role
{
    @Id
    private String name;
    @Column
    private String permissions;

    //    @ManyToOne
    //    private User user;


    @Override
    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    @Override
    public List<String> getPermissions()
    {
        return Arrays.asList( permissions.split( ";" ) );
    }
}
