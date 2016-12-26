package io.subutai.common.security.objects;


/**
 *
 */
public enum SecurityKeyType
{

    USER_KEY( 1, "User-Key" ),
    ENVIRONMENT_KEY( 2, "Environment-Key" ),
    PEER_OWNER_KEY( 3, "Peer-Owner-Key" ),
    PEER_KEY( 4, "Peer-Key" ),
    PEER_ENVIRONMENT_KEY( 5, "Peer-Environment-Key" ),
    MANAGEMENT_HOST_KEY( 6, "Management-Host-Key" ),
    RESOURCE_HOST_KEY( 8, "Resource-Host-Key" ),
    CONTAINER_HOST_KEY( 9, "Container-Host-Key" ),
    TEMPLATE_KEY( 10, "Template-Key" );

    private String name;
    private int id;


    SecurityKeyType( int id, String name )
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


    public static SecurityKeyType getById( int keyId )
    {
        if ( USER_KEY.getId() == keyId )
        {
            return USER_KEY;
        }
        else if ( ENVIRONMENT_KEY.getId() == keyId )
        {
            return ENVIRONMENT_KEY;
        }
        else if ( PEER_OWNER_KEY.getId() == keyId )
        {
            return PEER_OWNER_KEY;
        }
        else if ( PEER_KEY.getId() == keyId )
        {
            return PEER_KEY;
        }
        else if ( PEER_ENVIRONMENT_KEY.getId() == keyId )
        {
            return PEER_ENVIRONMENT_KEY;
        }
        else if ( MANAGEMENT_HOST_KEY.getId() == keyId )
        {
            return MANAGEMENT_HOST_KEY;
        }
        else if ( RESOURCE_HOST_KEY.getId() == keyId )
        {
            return RESOURCE_HOST_KEY;
        }
        else
        {
            return CONTAINER_HOST_KEY;
        }
    }
}
