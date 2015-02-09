package org.safehaus.subutai.core.identity.api;


/**
 * Created by talas on 2/5/15.
 */
public interface Permission
{
    public String getName();

    public PermissionGroup getPermissionGroup();

    public String getDescription();


    public void setName( String permissionKey );

    public void setPermissionGroup( PermissionGroup permissionGroup );

    public void setDescription( String description );
}
