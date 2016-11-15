package io.subutai.common.security.objects;


/**
 *
 */
public enum PermissionOperation
{
    READ( 1, "Read" ),
    WRITE( 2, "Write" ),
    UPDATE( 3, "Update" ),
    DELETE( 4, "Delete" );

    private String name;
    private int id;


    PermissionOperation( int id, String name )
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


    public static PermissionOperation getByName( String name )
    {
        if ( PermissionOperation.DELETE.getName().equals( name ) )
        {
            return PermissionOperation.DELETE;
        }
        else if ( PermissionOperation.READ.getName().equals( name ) )
        {
            return PermissionOperation.READ;
        }
        else if ( PermissionOperation.UPDATE.getName().equals( name ) )
        {
            return PermissionOperation.UPDATE;
        }
        else
        {
            return PermissionOperation.WRITE;
        }
    }
}
