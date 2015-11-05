package io.subutai.core.identity.impl.entity;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import io.subutai.core.identity.api.CliCommand;
import io.subutai.core.identity.api.Permission;
import io.subutai.core.identity.api.PortalModuleScope;
import io.subutai.core.identity.api.RestEndpointScope;
import io.subutai.core.identity.api.Role;
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

    @ManyToMany( targetEntity = PermissionEntity.class, fetch = FetchType.EAGER )
    @JoinTable( name = "subutai_role_permission", joinColumns = @JoinColumn( name = "role_name", referencedColumnName
            = "name" ), inverseJoinColumns = {
            @JoinColumn( name = "permission_name", referencedColumnName = "permission_name" ),
            @JoinColumn( name = "permission_group", referencedColumnName = "permission_group" )
    } )
    private Set<PermissionEntity> permissions = new HashSet<>();

    @ManyToMany( targetEntity = PortalModuleScopeEntity.class, fetch = FetchType.EAGER )
    @JoinTable( name = "subutai_role_module", joinColumns = @JoinColumn( name = "role_name", referencedColumnName =
            "name" ), inverseJoinColumns = @JoinColumn( name = "module_key", referencedColumnName = "module_key" ) )
    Set<PortalModuleScopeEntity> accessibleModules = new HashSet<>();


    @ManyToMany( targetEntity = RestEndpointScopeEntity.class, fetch = FetchType.EAGER )
    @JoinTable( name = "subutai_role_rest", joinColumns = @JoinColumn( name = "role_name", referencedColumnName =
            "name" ), inverseJoinColumns = {
            @JoinColumn( name = "rest_endpoint", referencedColumnName = "rest_endpoint" )
    } )
    Set<RestEndpointScopeEntity> accessibleRestEndpoints = new HashSet<>();


    @ManyToMany( targetEntity = CliCommandEntity.class, fetch = FetchType.EAGER )
    @JoinTable( name = "subutai_role_cli", joinColumns = @JoinColumn( name = "role_name", referencedColumnName =
            "name" ), inverseJoinColumns = {
            @JoinColumn( name = "cli_scope", referencedColumnName = "cli_scope" ),
            @JoinColumn( name = "cli_name", referencedColumnName = "cli_name" )
    } )
    List<CliCommandEntity> cliCommands = new ArrayList<>();


    public RoleEntity()
    {
    }


    public RoleEntity( final String name )
    {
        this.name = name;
    }


    public RoleEntity( final String name, final Set<PermissionEntity> permissions )
    {

        this.name = name;
        this.permissions = permissions;
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
        else
        {
            // @todo need to arrange with core (from frontend)
            CliCommandEntity entity = new CliCommandEntity( cliCommand.getScope(), cliCommand.getName() );
            cliCommands.add(entity);
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
    public void clearCliCommands()
    {
        cliCommands.clear();
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

        if ( module instanceof PortalModuleScopeEntity )
        {
            //throw new IllegalArgumentException( "Module is not instance of PortalModuleScopeEntity" );
            accessibleModules.add( ( PortalModuleScopeEntity ) module );
        }
        else
        {
            // @todo need to arrange with core (from frontend)
            PortalModuleScopeEntity entity = new PortalModuleScopeEntity( module.getModuleKey(), module.getModuleName() );
            accessibleModules.add(entity);
        }
        //LOG.debug( "Adding accessible module to role", module.getModuleName() );

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
        if ( endpointScope instanceof RestEndpointScopeEntity )
        {
            //throw new ClassCastException( "RestEndpointScope is not instance of RestEndpointScopeEntity" );
            accessibleRestEndpoints.add( ( RestEndpointScopeEntity ) endpointScope );
        }
        else
        {
            // @todo need to arrange with core (from frontend)
            RestEndpointScopeEntity entity = new RestEndpointScopeEntity( endpointScope.getRestEndpoint() );
            accessibleRestEndpoints.add(entity);
        }
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
