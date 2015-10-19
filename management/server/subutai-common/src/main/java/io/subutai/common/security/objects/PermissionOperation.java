package io.subutai.common.security.objects;


/**
 *
 */
public enum PermissionOperation
{
    All(1,"All"),
    Read(2,"Read"),
    Write(3,"Write"),
    Update(4,"Update"),
    Delete(5,"Delete"),
    Execute(6,"Execute");

    private String name;
    private int id;

    private PermissionOperation(  int id, String name)
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
