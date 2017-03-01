package io.subutai.common.protocol;


public class ReservedPort
{
    private Protocol protocol;
    private int port;


    public ReservedPort( final Protocol protocol, final int port )
    {
        this.protocol = protocol;
        this.port = port;
    }


    public int getPort()
    {
        return port;
    }


    public Protocol getProtocol()
    {
        return protocol;
    }
}
