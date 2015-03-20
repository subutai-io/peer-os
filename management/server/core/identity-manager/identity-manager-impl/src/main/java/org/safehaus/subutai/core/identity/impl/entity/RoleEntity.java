package org.safehaus.subutai.core.identity.impl.entity;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

import org.safehaus.subutai.core.identity.api.CliCommand;
import org.safehaus.subutai.core.identity.api.Permission;
import org.safehaus.subutai.core.identity.api.PortalModuleScope;
import org.safehaus.subutai.core.identity.api.RestEndpointScope;
import org.safehaus.subutai.core.identity.api.Role;
import org.safehaus.subutai.core.identity.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of Role interface.
 */
@Entity
@Table( name = "subutai_role" )
@Access( AccessType.FIELD )
public class RoleEntity implements Role
{
    private static final Logger LOG = LoggerFactory.getLogger( RoleEntity.class );
    @Id
    private String name;

    @Column( name = "permissions" )
    @OneToMany( fetch = FetchType.EAGER, cascade = { CascadeType.ALL } )
    private Set<PermissionEntity> permissions = new HashSet<>();


    @ManyToMany( targetEntity = UserEntity.class )
    @JoinTable( name = "subutai_user_role", joinColumns = @JoinColumn( name = "role_name", referencedColumnName =
            "name" ), inverseJoinColumns = @JoinColumn( name = "user_id", referencedColumnName = "user_id" ) )
    Set<User> users = new HashSet<>();


    @OneToMany( fetch = FetchType.EAGER, cascade = {
            CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH
    } )
    @Column( name = "accessible_modules" )
    Set<PortalModuleScopeEntity> accessibleModules = new HashSet<>();


    @OneToMany( fetch = FetchType.EAGER, cascade = CascadeType.ALL )
    @Column( name = "accessible_rest_endpoints" )
    Set<RestEndpointScopeEntity> accessibleRestEndpoints = new HashSet<>();


    @OneToMany( fetch = FetchType.EAGER, cascade = CascadeType.ALL )
    @Column( name = "accessible_cli_commands" )
    List<CliCommandEntity> cliCommands = new ArrayList<>();


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
    public List<CliCommand> getCliCommands()
    {
        List<CliCommand> cliCommandSet = new ArrayList<>();
        cliCommandSet.addAll( cliCommands );
        return cliCommandSet;
    }


    @Override
    public void addCliCommand( final CliCommand cliCommand )
    {
        if ( cliCommand instanceof CliCommandEntity )
        {
            cliCommands.add( ( CliCommandEntity ) cliCommand );
        }
    }


    @Override
    public void setCliCommands( final List<CliCommand> cliCommands )
    {
        this.cliCommands.clear();
        for ( final CliCommand cliCommand : cliCommands )
        {
            this.cliCommands.add( ( CliCommandEntity ) cliCommand );
        }
    }


    @Override
    public Set<PortalModuleScope> getAccessibleModules()
    {
        Set<PortalModuleScope> portalModules = new HashSet<>();
        portalModules.addAll( accessibleModules );
        return portalModules;
    }


    @Override
    public void addPortalModule( final PortalModuleScope module )
    {
        if ( module == null )
        {
            throw new IllegalArgumentException( "Module could not be null." );
        }
        if ( !( module instanceof PortalModuleScopeEntity ) )
        {
            throw new IllegalArgumentException( "Module is not instance of PortalModuleScopeEntity" );
        }
        LOG.debug( "Adding accessible module to role", module.getModuleName() );
        accessibleModules.add( ( PortalModuleScopeEntity ) module );
    }


    @Override
    public void clearPortalModules()
    {
        accessibleModules.clear();
    }


    @Override
    public void addRestEndpointScope( final RestEndpointScope endpointScope )
    {
        if ( endpointScope == null )
        {
            throw new IllegalArgumentException( "Endpoint cannot be null" );
        }
        if ( !( endpointScope instanceof RestEndpointScopeEntity ) )
        {
            throw new ClassCastException( "RestEndpointScope is not instance of RestEndpointScopeEntity" );
        }
        accessibleRestEndpoints.add( ( RestEndpointScopeEntity ) endpointScope );
    }


    @Override
    public Set<RestEndpointScope> getAccessibleRestEndpoints()
    {
        Set<RestEndpointScope> restEndpointScopes = new HashSet<>();
        restEndpointScopes.addAll( accessibleRestEndpoints );
        return restEndpointScopes;
    }


    @Override
    public void clearRestEndpointScopes()
    {
        accessibleRestEndpoints.clear();
    }


    @Override
    public boolean canAccessModule( final String moduleKey )
    {
        for ( final PortalModuleScopeEntity accessibleModule : accessibleModules )
        {
            if ( moduleKey != null && moduleKey.equalsIgnoreCase( accessibleModule.getModuleKey() ) )
            {
                return true;
            }
        }
        return false;
    }
}
