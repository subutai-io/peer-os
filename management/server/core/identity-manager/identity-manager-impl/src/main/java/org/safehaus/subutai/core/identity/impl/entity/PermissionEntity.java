package org.safehaus.subutai.core.identity.impl.entity;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;

import org.safehaus.subutai.core.identity.api.Permission;
import org.safehaus.subutai.core.identity.api.PermissionGroup;

import com.google.common.base.Preconditions;


@Entity
@Access( AccessType.FIELD )
@IdClass( PermissionPK.class )
public class PermissionEntity implements Permission, Serializable
{
    @Id
    @Column( name = "permission_name" )
    private String name;

    @Id
    @Column( name = "permission_group" )
    @Enumerated( EnumType.STRING )
    private PermissionGroup permissionGroup;

    @Column( name = "description" )
    private String description;


    public PermissionEntity()
    {
    }


    public PermissionEntity( final String name, final PermissionGroup permissionGroup, final String description )
    {
        Preconditions.checkNotNull( name, "PermissionName cannot be null." );
        Preconditions.checkNotNull( permissionGroup, "PermissionGroup cannot be null." );
        Preconditions.checkNotNull( description, "Permission description is null." );
        this.name = name;
        this.permissionGroup = permissionGroup;
        this.description = description;
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public PermissionGroup getPermissionGroup()
    {
        return permissionGroup;
    }


    @Override
    public String getDescription()
    {
        return description;
    }


    @Override
    public void setName( final String name )
    {
        Preconditions.checkNotNull( name, "Cannot set null value for name" );
        this.name = name;
    }


    @Override
    public void setPermissionGroup( final PermissionGroup permissionGroup )
    {
        Preconditions.checkNotNull( permissionGroup, "Cannot set null value for permissionGroup" );
        this.permissionGroup = permissionGroup;
    }


    @Override
    public void setDescription( final String description )
    {
        Preconditions.checkNotNull( description, "Cannot set null description" );
        this.description = description;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof PermissionEntity ) )
        {
            return false;
        }

        final PermissionEntity that = ( PermissionEntity ) o;

        return permissionGroup == that.permissionGroup && name.equals( that.name );
    }


    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + permissionGroup.hashCode();
        return result;
    }
}
