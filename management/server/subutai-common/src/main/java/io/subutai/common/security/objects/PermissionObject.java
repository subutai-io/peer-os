package io.subutai.common.security.objects;


/**
 *
 */
public enum PermissionObject
{
    // PermissionObject|read-write-update

    IDENTITY_MANAGEMENT( 1, "Identity-Management" ), PEER_MANAGEMENT( 2, "Peer-Management" ),
    ENVIRONMENT_MANAGEMENT( 3, "Environment-Management" ), RESOURCE_MANAGEMENT( 4, "Resource-Management" ),
    TEMPLATE_MANAGEMENT( 5, "Template-Management" ), KARAF_SERVER_ADMINISTRATION( 6, "Karaf-Server-Administration" ),
    SYSTEM_MANAGEMENT( 7, "System-Management" ), TENANT_MANAGEMENT( 8, "Tenant-Management" ), PLUGIN_MANAGEMENT( 9, "Plugin-Management" );


    private int id;
    private String name;


    PermissionObject( int id, String name )
    {
        this.id = id;
        this.name = name;
    }


    public String getName()
    {
        return name;
    }


    public int getId()
    {
        return id;
    }

}
