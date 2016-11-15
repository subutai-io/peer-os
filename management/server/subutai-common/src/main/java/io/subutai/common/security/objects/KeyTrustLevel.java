package io.subutai.common.security.objects;


/**
 * PGP Key trust level
 */
public enum KeyTrustLevel
{
    NEVER( 1, "Never Trust" ),
    MARGINAL( 2, "Marginal" ),
    FULL( 3, "Full" ),
    ULTIMATE( 4, "Ultimate Trust" );

    private String name;
    private int id;

    KeyTrustLevel(  int id, String name)
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
