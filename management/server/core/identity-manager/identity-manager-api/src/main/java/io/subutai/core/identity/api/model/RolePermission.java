package io.subutai.core.identity.api.model;


import java.util.List;


public interface RolePermission
{
    public Long getId();

    public void setId( Long id );

    public Long getPermissionId();

    public void setPermissionId( Long permissionId );

    public Long getRoleId();

    public void setRoleId( Long roleId );

    public int getScope();

    public void setScope( int scope );

    public boolean isRead();

    public void setRead( boolean read );

    public boolean isWrite();

    public void setWrite( boolean write );

    public boolean isUpdate();

    public void setUpdate( boolean update );

    public boolean isDelete();

    public void setDelete( boolean delete );

    // TODO: change role_object to object_name
    public String getObjectName();

    public void setObjectName( String obj );

    // TODO: change perm_object to object_type
    public int getPermObject();

    public void setPermObject( int permObject );

    public List<String> asString();
}
