package io.subutai.common.security.objects;


/**
 *
 */
public enum AuthType
{
    USERNAME(1, "Username" ),
    TOKEN(2, "Token" ),
    SIGNED_MESSAGE(3,"SignedMessage" );

    private String name;
    private int id;


    AuthType(  int id, String name)
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
