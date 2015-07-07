package io.subutai.core.identity.api;


public interface Permission
{
    public String getName();

    public PermissionGroup getPermissionGroup();

    public String getDescription();


    public void setName( String permissionKey );

    public void setPermissionGroup( PermissionGroup permissionGroup );

    public void setDescription( String description );
}
