package io.subutai.common.security.objects;


/**
 *
 */
public enum PermissionScope
{
    ALL_SCOPE( 1,"All-Objects" ),
    OWNER_SCOPE( 2, "Owner/Trusted-Objects" );

    private String name;
    private int id;

    PermissionScope(  int id, String name)
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
