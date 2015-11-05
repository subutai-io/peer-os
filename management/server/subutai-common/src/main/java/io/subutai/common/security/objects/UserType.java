package io.subutai.common.security.objects;


/**
 *
 */
public enum UserType
{
    Internal(1,"Internal"),
    System(2,"System");

    private String name;
    private int id;


    private UserType(  int id, String name)
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
