package io.subutai.common.security.objects;


/**
 *
 */
public enum SecurityKeyType
{

    UserKey(1,"User-Key"),
    EnvironmentKey(2,"Environment-Key"),
    PeerOwnerKey(3,"Peer-Owner-Key"),
    PeerKey(4,"Peer-Key"),
    PeerEnvironmentKey(5,"Peer-Environment-Key"),
    ManagementHostKey(6,"Management-Host-Key"),
    ResourceHostKey(8,"Resource-Host-Key"),
    ContainerHostKey(7,"Container-Host-Key");

    private String name;
    private int id;


    SecurityKeyType(  int id, String name)
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
