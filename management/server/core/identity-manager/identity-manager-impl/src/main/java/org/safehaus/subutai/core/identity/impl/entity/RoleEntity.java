package org.safehaus.subutai.core.identity.impl.entity;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.safehaus.subutai.core.identity.api.Permission;
import org.safehaus.subutai.core.identity.api.Role;
import org.safehaus.subutai.core.identity.api.User;


/**
 * Implementation of Role interface.
 */
@Entity
@Table( name = "subutai_role" )
@Access( AccessType.FIELD )
public class RoleEntity implements Role, Serializable
{
    @Id
    private String name;

    @Column( name = "permissions" )
    @OneToMany( fetch = FetchType.EAGER )
    private Set<PermissionEntity> permissions = new HashSet<>();


    @ManyToMany( targetEntity = UserEntity.class )
    @JoinTable( name = "subutai_user_role", joinColumns = @JoinColumn( name = "role_name", referencedColumnName =
            "name" ), inverseJoinColumns = @JoinColumn( name = "user_id", referencedColumnName = "user_id" ) )
    Set<User> users = new HashSet<>();


    public RoleEntity()
    {
    }


    public RoleEntity( final String name )
    {
        this.name = name;
    }


    public RoleEntity( final String name, final Set<PermissionEntity> permissions, final Set<User> users )
    {

        this.name = name;
        this.permissions = permissions;
        this.users = users;
    }


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
    public Set<Permission> getPermissions()
    {
        Set<Permission> permissionSet = new HashSet<>();
        permissionSet.addAll( permissions );
        return permissionSet;
    }


    @Override
    public void addPermission( final Permission permission )
    {
        if ( !( permission instanceof PermissionEntity ) )
        {
            return;
        }

        PermissionEntity permissionEntity = ( PermissionEntity ) permission;
        permissions.add( permissionEntity );
    }


    @Override
    public void removePermission( final Permission permission )
    {
        if ( !( permission instanceof PermissionEntity ) )
        {
            return;
        }

        PermissionEntity permissionEntity = ( PermissionEntity ) permission;
        permissions.remove( permissionEntity );
    }
}
