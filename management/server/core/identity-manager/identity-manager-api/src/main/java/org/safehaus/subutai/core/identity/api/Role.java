package org.safehaus.subutai.core.identity.api;


import java.io.Serializable;
import java.util.List;
import java.util.Set;


public interface Role extends Serializable
{
    public String getName();

    public Set<Permission> getPermissions();

    public void addPermission( Permission permission );

    public void removePermission( Permission permission );

    public List<CliCommand> getCliCommands();

    public void addCliCommand( CliCommand cliCommand );

    public void setCliCommands( List<CliCommand> cliCommands );

    public void addPortalModule( PortalModuleScope portalModule );

    public Set<PortalModuleScope> getAccessibleModules();

    public void clearPortalModules();

    public void addRestEndpointScope(RestEndpointScope endpointScope);

    public Set<RestEndpointScope> getAccessibleRestEndpoints();

    public void clearRestEndpointScopes();

    public boolean canAccessModule( String moduleKey );
}
