package io.subutai.common.security.objects;


/**
 *
 */
public enum PermissionOperationType
{
    All("All"),
    Read("Read"),
    Write("Write"),
    Update("Update"),
    Delete("Delete"),
    Execute("Execute");

    private String name;

    private PermissionOperationType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
