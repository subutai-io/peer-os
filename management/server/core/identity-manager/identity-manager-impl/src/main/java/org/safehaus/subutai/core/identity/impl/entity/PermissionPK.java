package org.safehaus.subutai.core.identity.impl.entity;


import java.io.Serializable;


/**
 * Created by talas on 2/4/15.
 */

public class PermissionPK implements Serializable
{
    private String permissionKey;
    private PermissionGroup permissionGroup;


    public PermissionPK()
    {
    }


    public PermissionPK( final String permissionKey, final PermissionGroup permissionGroup )
    {
        this.permissionKey = permissionKey;
        this.permissionGroup = permissionGroup;
    }


    public String getPermissionKey()
    {
        return permissionKey;
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

        return permissionGroup == that.permissionGroup && permissionKey.equals( that.permissionKey );
    }


    @Override
    public int hashCode()
    {
        int result = permissionKey.hashCode();
        result = 31 * result + permissionGroup.hashCode();
        return result;
    }
}
