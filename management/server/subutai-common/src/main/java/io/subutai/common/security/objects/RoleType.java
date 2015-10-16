package io.subutai.common.security.objects;


/**
 *
 */
public enum RoleType
{
    Administrator("Administrator"),
    Manager("Manager"),
    IdentityManager("Identity-Manager"),
    PeerManager("Peer-Manager"),
    EnvironmentManager("Environment-Manager"),
    ResourceManager("Resource-Manager"),
    TemplateManager("Template-Manager"),
    KarafServerAdmin("Karaf-Server-Admin"),
    KarafServerManager("Karaf-Server-Manager");

    private String name;

    private RoleType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
