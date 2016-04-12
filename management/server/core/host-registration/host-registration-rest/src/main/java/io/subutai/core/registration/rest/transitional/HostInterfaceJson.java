package io.subutai.core.registration.rest.transitional;


import com.google.common.base.Preconditions;

import io.subutai.common.host.HostInterface;


public class HostInterfaceJson implements HostInterface
{
    private String interfaceName;
    private String ip;


    public HostInterfaceJson( final HostInterface aHostInterface )
    {
        Preconditions.checkNotNull( aHostInterface, "Invalid null argument aInterface" );
        this.interfaceName = aHostInterface.getName();
        this.ip = aHostInterface.getIp();
    }


    @Override
    public String getName()
    {
        return interfaceName;
    }


    @Override
    public String getIp()
    {
        return ip;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof HostInterfaceJson ) )
        {
            return false;
        }

        final HostInterfaceJson that = ( HostInterfaceJson ) o;

        return interfaceName.equals( that.interfaceName ) && ip.equals( that.ip );
    }


    @Override
    public int hashCode()
    {
        int result = interfaceName.hashCode();
        result = 31 * result + ip.hashCode();
        return result;
    }
}
