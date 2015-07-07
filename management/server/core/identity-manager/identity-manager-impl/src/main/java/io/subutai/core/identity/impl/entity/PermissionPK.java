package io.subutai.core.identity.impl.entity;


import java.io.Serializable;

import io.subutai.core.identity.api.PermissionGroup;

import com.google.common.base.Preconditions;


public class PermissionPK implements Serializable
{
    private String name;
    private PermissionGroup permissionGroup;


    public PermissionPK()
    {
    }


    public PermissionPK( final String permissionKey, final PermissionGroup permissionGroup )
    {
        Preconditions.checkNotNull( permissionKey, "Permission Name cannot be null value." );
        Preconditions.checkNotNull( permissionGroup, "Ya know it's not applicable to set permissionGroup to null" );
        this.name = permissionKey;
        this.permissionGroup = permissionGroup;
    }


    public String getPermissionKey()
    {
        return name;
    }


    public PermissionGroup getPermissionGroup()
    {
        return permissionGroup;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof PermissionPK ) )
        {
            return false;
        }

        final PermissionPK that = ( PermissionPK ) o;

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
