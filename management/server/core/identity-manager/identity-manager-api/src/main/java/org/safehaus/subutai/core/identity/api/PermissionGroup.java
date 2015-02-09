package org.safehaus.subutai.core.identity.api;


/**
 * Created by talas on 2/4/15.
 */
public enum PermissionGroup
{
    ENVIRONMENT_PERMISSIONS( "Environment group" ),
    PEER_PERMISSIONS( "Peer group" ),
    TEMPLATE_REGISTRY_PERMISSIONS( "Template registry group" ),
    DEFAULT_PERMISSIONS( "Default system permissions" );

    private String name;


    private PermissionGroup( String name )
    {
        this.name = name;
    }


    public String getName()
    {
        return name;
    }
}
