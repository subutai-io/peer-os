package io.subutai.bazaar.share.dto.environment.container;


public class ContainerDesktopInfoDto
{
    private String id;

    private String type;

    private String rdServer;


    public ContainerDesktopInfoDto()
    {
    }


    public ContainerDesktopInfoDto( final String id, final String type, final String rdServer )
    {
        this.id = id;
        this.type = type;
        this.rdServer = rdServer;
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getType()
    {
        return type;
    }


    public void setType( final String type )
    {
        this.type = type;
    }


    public String getRdServer()
    {
        return rdServer;
    }


    public void setRdServer( final String rdServer )
    {
        this.rdServer = rdServer;
    }
}

