package io.subutai.common.security.objects;


/**
 *
 */
public enum PermissionObjectType
{
    UserManagement( "User-Management" ),
    PeerManagement( "Peer-Management" ),
    EnvironmentManagement("Environment-Management" ),
    ResourceManagement( "Resource-Management" ),
    TemplateManagement( "Template-Management" ),
    KarafServerManagement( "Karaf-Server-Management" );

    private String name;


    private PermissionObjectType( String name )
    {
        this.name = name;
    }


    public String getName()
    {
        return name;
    }
}
