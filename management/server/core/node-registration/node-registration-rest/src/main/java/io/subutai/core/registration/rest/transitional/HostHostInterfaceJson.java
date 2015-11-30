package io.subutai.core.registration.rest.transitional;


import com.google.common.base.Preconditions;

import io.subutai.common.host.HostInterface;



public class HostHostInterfaceJson implements HostInterface
{
    private String interfaceName;
    private String ip;
    private String mac;


    public HostHostInterfaceJson( final HostInterface aHostInterface )
    {
        Preconditions.checkNotNull( aHostInterface, "Invalid null argument aInterface" );
        this.interfaceName = aHostInterface.getName();
        this.ip = aHostInterface.getIp();
        this.mac = aHostInterface.getMac();
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
    public String getMac()
    {
        return mac;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof HostHostInterfaceJson ) )
        {
            return false;
        }

        final HostHostInterfaceJson that = ( HostHostInterfaceJson ) o;

        return interfaceName.equals( that.interfaceName ) && ip.equals( that.ip ) && mac.equals( that.mac );
    }


    @Override
    public int hashCode()
    {
        int result = interfaceName.hashCode();
        result = 31 * result + ip.hashCode();
        result = 31 * result + mac.hashCode();
        return result;
    }
}
