package io.subutai.common.security.objects;


/**
 *
 */
public enum RoleType
{
    Administrator(1,"Administrator"),
    Manager(2,"Manager");

    private String name;
    private int id;


    private RoleType(  int id, String name)
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
