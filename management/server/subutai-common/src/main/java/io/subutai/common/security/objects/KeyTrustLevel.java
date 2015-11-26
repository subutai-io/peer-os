package io.subutai.common.security.objects;


/**
 * PGP Key trust level
 */
public enum KeyTrustLevel
{
    Never(1,"Never Trust"),
    Marginal(2,"Never Trust"),
    Full(3,"Never Trust"),
    Ultimate(4,"Never Trust");

    private String name;
    private int id;

    private KeyTrustLevel(  int id, String name)
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
