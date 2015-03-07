package org.safehaus.subutai.core.identity.impl.entity;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
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
import org.safehaus.subutai.core.identity.api.UserPortalModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of Role interface.
 */
@Entity
@Table( name = "subutai_role" )
@Access( AccessType.FIELD )
public class RoleEntity implements Role, Serializable
{
    private static final Logger LOG = LoggerFactory.getLogger( RoleEntity.class );
    @Id
    private String name;

    @Column( name = "permissions" )
    @OneToMany( fetch = FetchType.EAGER )
    private Set<PermissionEntity> permissions = new HashSet<>();


    @ManyToMany( targetEntity = UserEntity.class )
    @JoinTable( name = "subutai_user_role", joinColumns = @JoinColumn( name = "role_name", referencedColumnName =
            "name" ), inverseJoinColumns = @JoinColumn( name = "user_id", referencedColumnName = "user_id" ) )
    Set<User> users = new HashSet<>();


    @OneToMany( fetch = FetchType.EAGER, cascade = {
            CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH
    } )
    @Column( name = "accessible_modules" )
    Set<UserPortalModuleEntity> accessibleModules = new HashSet<>();


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


    @Override
    public Set<UserPortalModule> getAccessibleModules()
    {
        Set<UserPortalModule> portalModules = new HashSet<>();
        portalModules.addAll( accessibleModules );
        return portalModules;
    }


    @Override
    public void addPortalModule( final UserPortalModule module )
    {
        if ( module == null )
        {
            throw new IllegalArgumentException( "Module could not be null." );
        }
        if ( !( module instanceof UserPortalModuleEntity ) )
        {
            throw new IllegalArgumentException( "Module is not instance of UserPortalModuleEntity" );
        }
        LOG.debug( "Adding accessible module to role", module.getModuleName() );
        accessibleModules.add( ( UserPortalModuleEntity ) module );
    }


    @Override
    public void clearPortalModules()
    {
        accessibleModules.clear();
    }


    @Override
    public boolean canAccessModule( final String moduleKey )
    {
        for ( final UserPortalModuleEntity accessibleModule : accessibleModules )
        {
            if ( moduleKey != null && moduleKey.equalsIgnoreCase( accessibleModule.getModuleKey() ) )
            {
                return true;
            }
        }
        return false;
    }
}
