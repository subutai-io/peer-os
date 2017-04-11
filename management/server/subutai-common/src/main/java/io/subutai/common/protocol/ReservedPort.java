package io.subutai.common.protocol;


public class ReservedPort
{
    private Protocol protocol;
    private int port;
    private String containerIpPort;


    public ReservedPort( final Protocol protocol, final int port )
    {
        this.protocol = protocol;
        this.port = port;
    }


    public ReservedPort( final Protocol protocol, final int port, final String containerIpPort )
    {
        this( protocol, port );
        this.containerIpPort = containerIpPort;
    }


    public int getPort()
    {
        return port;
    }


    public Protocol getProtocol()
    {
        return protocol;
    }


    public String getContainerIpPort()
    {
        return containerIpPort;
    }
}
