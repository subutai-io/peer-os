package io.subutai.common.security.objects;


/**
 *
 */
public enum UserType
{
    System(1,"System"),
    Regular(2,"Regular");

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
