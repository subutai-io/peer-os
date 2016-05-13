package io.subutai.common.network;


import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class SshTunnel
{
    private final String ip;

    private final int port;


    public SshTunnel( final String ip, final int port )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ) );
        Preconditions.checkArgument( port > 0 );

        this.ip = ip;
        this.port = port;
    }


    public String getIp()
    {
        return ip;
    }


    public int getPort()
    {
        return port;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "ip", ip ).add( "port", port ).toString();
    }
}
