package io.subutai.core.registration.rest.transitional;


import com.google.common.base.Preconditions;

import io.subutai.common.host.Interface;


/**
 * Created by talas on 9/18/15.
 */
public class HostInterfaceJson implements Interface
{
    private String interfaceName;
    private String ip;
    private String mac;


    public HostInterfaceJson( final Interface aInterface )
    {
        Preconditions.checkNotNull( aInterface, "Invalid null argument aInterface" );
        this.interfaceName = aInterface.getInterfaceName();
        this.ip = aInterface.getIp();
        this.mac = aInterface.getMac();
    }


    @Override
    public String getInterfaceName()
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
        if ( !( o instanceof HostInterfaceJson ) )
        {
            return false;
        }

        final HostInterfaceJson that = ( HostInterfaceJson ) o;

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
