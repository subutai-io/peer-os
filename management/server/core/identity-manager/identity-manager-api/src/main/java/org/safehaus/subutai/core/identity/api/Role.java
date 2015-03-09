package org.safehaus.subutai.core.identity.api;


import java.io.Serializable;
import java.util.Set;


/**
 * Created by timur on 1/21/15.
 */
public interface Role extends Serializable
{
    public String getName();

    public Set<Permission> getPermissions();

    public void addPermission( Permission permission );

    public void removePermission( Permission permission );

    public void addPortalModule( PortalModuleScope portalModule );

    public Set<PortalModuleScope> getAccessibleModules();

    public void clearPortalModules();

    public void addRestEndpointScope(RestEndpointScope endpointScope);

    public Set<RestEndpointScope> getAccessibleRestEndpoints();

    public void clearRestEndpointScopes();

    public boolean canAccessModule( String moduleKey );
}
