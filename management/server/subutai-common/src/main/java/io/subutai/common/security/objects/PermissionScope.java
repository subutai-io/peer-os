package io.subutai.common.security.objects;


/**
 *
 */
public enum PermissionScope
{
    ALL_SCOPE( 1,"All-Objects" ),
    CHILD_SCOPE( 2, "Child-Objects" ),
    OWNER_SCOPE( 3, "Owner-Objects" );

    private String name;
    private int id;


    private PermissionScope(  int id, String name)
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
