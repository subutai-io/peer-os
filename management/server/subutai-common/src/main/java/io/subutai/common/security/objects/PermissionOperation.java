package io.subutai.common.security.objects;


/**
 *
 */
public enum PermissionOperation
{
    Read(1,"Read"),
    Write(2,"Write"),
    Update(3,"Update"),
    Delete(4,"Delete");

    private String name;
    private int id;

    PermissionOperation(  int id, String name)
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
