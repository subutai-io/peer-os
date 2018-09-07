package io.subutai.bazaar.share.dto.domain;


public class ReservedPortMapping
{
    private PortMapDto.Protocol protocol;
    private int externalPort;
    private String ipAddress;
    private int internalPort;
    private String domain;


    public ReservedPortMapping()
    {
    }


    public PortMapDto.Protocol getProtocol()
    {
        return protocol;
    }


    public void setProtocol( final PortMapDto.Protocol protocol )
    {
        this.protocol = protocol;
    }


    public int getExternalPort()
    {
        return externalPort;
    }


    public void setExternalPort( final int externalPort )
    {
        this.externalPort = externalPort;
    }


    public String getIpAddress()
    {
        return ipAddress;
    }


    public void setIpAddress( final String ipAddress )
    {
        this.ipAddress = ipAddress;
    }


    public int getInternalPort()
    {
        return internalPort;
    }


    public void setInternalPort( final int internalPort )
    {
        this.internalPort = internalPort;
    }


    public String getDomain()
    {
        return domain;
    }


    public void setDomain( final String domain )
    {
        this.domain = domain;
    }
}
