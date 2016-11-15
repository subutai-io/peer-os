package io.subutai.common.security.objects;


/**
 *
 */
public enum TokenType
{
    SESSION(1,"Session"),
    PERMANENT(2,"Permanent");

    private String name;
    private int id;


    TokenType(  int id, String name)
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
