package io.subutai.common.security.objects;


/**
 *
 */
public enum UserStatus
{
    ACTIVE(1,"ACTIVE"),
    DISABLED(2,"Disabled");

    private String name;
    private int id;


    UserStatus(  int id, String name)
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
