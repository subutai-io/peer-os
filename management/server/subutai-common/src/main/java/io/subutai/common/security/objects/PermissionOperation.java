package io.subutai.common.security.objects;


/**
 *
 */
public enum PermissionOperation
{
    Read( 1, "Read" ),
    Write( 2, "Write" ),
    Update( 3, "Update" ),
    Delete( 4, "Delete" );

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
        if ( PermissionOperation.Delete.getName().equals( name ) )
        {
            return PermissionOperation.Delete;
        }
        else if ( PermissionOperation.Read.getName().equals( name ) )
        {
            return PermissionOperation.Read;
        }
        else if ( PermissionOperation.Update.getName().equals( name ) )
        {
            return PermissionOperation.Update;
        }
        else
        {
            return PermissionOperation.Write;
        }
    }
}
