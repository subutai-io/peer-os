package io.subutai.common.security.objects;


/**
 *
 */
public enum AuthType
{
    Username(1, "Username" ),
    Token(2, "Token" ),
    SignedMessage(3,"SignedMessage" );

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
