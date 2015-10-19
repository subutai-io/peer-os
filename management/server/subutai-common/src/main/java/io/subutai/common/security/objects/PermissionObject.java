package io.subutai.common.security.objects;


/**
 *
 */
public enum PermissionObject
{
    UserManagement(1, "User-Management" ),
    PeerManagement(2, "Peer-Management" ),
    EnvironmentManagement(3,"Environment-Management" ),
    ResourceManagement(4, "Resource-Management" ),
    TemplateManagement(5, "Template-Management" ),
    KarafServerManagement(6, "Karaf-Server-Management" );

    private String name;
    private int id;


    private PermissionObject(  int id, String name)
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
