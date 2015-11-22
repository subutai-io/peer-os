package io.subutai.core.identity.rest.ui;

public class PermissionJson {
    private int object;
    private int scope;
    private boolean read;
    private boolean write;
    private boolean update;
    private boolean delete;

    public PermissionJson( final int object, final int scope, final boolean read,
                          final boolean write, final boolean update, final boolean delete )
    {
        this.object = object;
        this.scope = scope;
        this.read = read;
        this.write = write;
        this.update = update;
        this.delete = delete;
    }

    public int getObject()
    {
        return object;
    }

    public void setObjectId( final int object )
    {
        this.object = object;
    }

    public int getScope()
    {
        return scope;
    }

    public void setScope( final int scope )
    {
        this.scope = scope;
    }

    public boolean getRead()
    {
        return read;
    }

    public void setRead( final boolean read )
    {
        this.read = read;
    }

    public boolean getWrite()
    {
        return write;
    }

    public void setWrite( final boolean write )
    {
        this.write = write;
    }

    public boolean getUpdate()
    {
        return update;
    }

    public void setUpdate( final boolean update )
    {
        this.update = update;
    }

    public boolean getDelete()
    {
        return delete;
    }

    public void setDelete( final boolean delete )
    {
        this.delete = delete;
    }
}
