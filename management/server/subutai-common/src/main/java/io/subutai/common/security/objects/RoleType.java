package io.subutai.common.security.objects;


/**
 *
 */
public enum RoleType
{
    Internal(1,"Internal System Manager"),
    KarafManager(2,"Karaf-Manager"),
    Administrator(3,"Administrator"),
    Manager(4,"Manager");

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
