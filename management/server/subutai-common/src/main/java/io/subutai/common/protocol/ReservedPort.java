package io.subutai.common.protocol;


public class ReservedPort
{
    private Protocol protocol;
    private int port;
    private String containerIp;
    private int containerPort;
    private String domain;


    public ReservedPort( final Protocol protocol, final int port )
    {
        this.protocol = protocol;
        this.port = port;
    }


    public ReservedPort( final Protocol protocol, final int port, final String containerIp, final int containerPort,
                         final String domain )
    {
        this( protocol, port );
        this.containerIp = containerIp;
        this.containerPort = containerPort;
        this.domain = domain;
    }


    public int getPort()
    {
        return port;
    }


    public Protocol getProtocol()
    {
        return protocol;
    }


    public String getContainerIp()
    {
        return containerIp;
    }


    public int getContainerPort()
    {
        return containerPort;
    }


    public String getDomain()
    {
        return domain;
    }
}
