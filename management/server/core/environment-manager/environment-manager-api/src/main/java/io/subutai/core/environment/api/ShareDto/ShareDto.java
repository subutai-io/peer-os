package io.subutai.core.environment.api.ShareDto;


public class ShareDto
{
    private Long id;
    private boolean read;
    private boolean write;
    private boolean update;
    private boolean delete;


    public Long getId()
    {
        return id;
    }


    public void setId( final Long id )
    {
        this.id = id;
    }


    public boolean isRead()
    {
        return read;
    }


    public void setRead( final boolean read )
    {
        this.read = read;
    }


    public boolean isWrite()
    {
        return write;
    }


    public void setWrite( final boolean write )
    {
        this.write = write;
    }


    public boolean isUpdate()
    {
        return update;
    }


    public void setUpdate( final boolean update )
    {
        this.update = update;
    }


    public boolean isDelete()
    {
        return delete;
    }


    public void setDelete( final boolean delete )
    {
        this.delete = delete;
    }
}
