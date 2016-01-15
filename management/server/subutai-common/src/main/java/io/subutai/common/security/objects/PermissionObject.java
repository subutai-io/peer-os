package io.subutai.common.security.objects;


/**
 *
 */
public enum PermissionObject
{
    // PermissionObject|all|read-write-update 
	
	IdentityManagement(1, "Identity-Management" ),
    PeerManagement(2, "Peer-Management" ),
    EnvironmentManagement(3,"Environment-Management" ),
    ResourceManagement(4, "Resource-Management" ),
    TemplateManagement(5, "Template-Management" ),
    KarafServerAdministration(6, "Karaf-Server-Administration" ),
    KarafServerManagement(7, "Karaf-Server-Management" );

    private String name;
    private int id;


    PermissionObject(  int id, String name)
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
