package io.subutai.common.security.objects;


/**
 *
 */
public enum UserStatus
{
    Active(1,"Active"),
    Disabled(2,"Disabled");

    private String name;
    private int id;


    private UserStatus(  int id, String name)
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
