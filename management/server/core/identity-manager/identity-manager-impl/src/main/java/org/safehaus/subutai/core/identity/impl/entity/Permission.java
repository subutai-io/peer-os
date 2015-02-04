package org.safehaus.subutai.core.identity.impl.entity;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;


/**
 * Created by talas on 2/4/15.
 */
@Entity
@Access( AccessType.FIELD )
@IdClass( PermissionPK.class )
public class Permission
{
    @Id
    @Column( name = "permission_key" )
    private String permissionKey;

    @Id
    @Column( name = "permission_group" )
    @Enumerated( EnumType.STRING )
    private PermissionGroup permissionGroup;

    @Column( name = "description" )
    private String description;


    public Permission( final String permissionKey, final PermissionGroup permissionGroup, final String description )
    {
        this.permissionKey = permissionKey;
        this.permissionGroup = permissionGroup;
        this.description = description;
    }


    public String getPermissionKey()
    {
        return permissionKey;
    }


    public PermissionGroup getPermissionGroup()
    {
        return permissionGroup;
    }


    public String getDescription()
    {
        return description;
    }
}
