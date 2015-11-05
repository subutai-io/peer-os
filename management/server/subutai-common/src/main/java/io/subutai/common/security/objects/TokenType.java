package io.subutai.common.security.objects;


/**
 *
 */
public enum TokenType
{
    Session(1,"Session"),
    Permanent(2,"Permanent");

    private String name;
    private int id;


    private TokenType(  int id, String name)
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
