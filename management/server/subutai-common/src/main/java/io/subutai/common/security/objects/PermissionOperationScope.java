package io.subutai.common.security.objects;


/**
 *
 */
public enum PermissionOperationScope
{
    ALL_SCOPE( "All Objects" ),
    CHILD_SCOPE( "Child Objects" ),
    OWNER_SCOPE( "Owner Objects" );

    private String name;


    private PermissionOperationScope( String name )
    {
        this.name = name;
    }


    public String getName()
    {
        return name;
    }
}
