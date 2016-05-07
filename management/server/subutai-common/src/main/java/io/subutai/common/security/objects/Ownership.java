package io.subutai.common.security.objects;


public enum Ownership
{
    ALL( 1, "All" ),
    GROUP( 2, "Group" ),
    USER( 3, "Owner" );

    private String name;
    private int id;


    Ownership( int id, String name )
    {
        this.id = id;
        this.name = name;
    }


    public String getName()
    {
        return name;
    }


    public int getLevel()
    {
        return id;
    }


    public static Ownership getByName( String name )
    {
        if ( Ownership.USER.getName().equals( name ) )
        {
            return Ownership.USER;
        }
        else if ( Ownership.GROUP.getName().equals( name ) )
        {
            return Ownership.GROUP;
        }
        else
        {
            return Ownership.ALL;
        }
    }
}
