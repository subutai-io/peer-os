package io.subutai.bazaar.share.dto;


public class PeerInfoDto
{
    private String id;

    private String name;

    private String version;

    private String scope;


    public PeerInfoDto()
    {
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getVersion()
    {
        return version;
    }


    public void setVersion( final String version )
    {
        this.version = version;
    }


    public String getScope()
    {
        return scope;
    }


    public void setScope( final String scope )
    {
        this.scope = scope;
    }
}
