package io.subutai.core.kurjun.manager.impl.model;


/**
 *
 */
public enum KurjunType
{
    Local(1,"Local"),
    Global(2,"Global"),
    Custom(3,"Custom");

    private String name;
    private int id;


    private KurjunType(  int id, String name)
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
