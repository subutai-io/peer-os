package io.subutai.core.hostregistry.impl;


import org.safehaus.subutai.common.host.Interface;

import com.google.common.base.Objects;


/**
 * Network interface
 */
public class InterfaceImpl implements Interface
{

    private String interfaceName;
    private String ip;
    private String mac;


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
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "interfaceName", interfaceName ).add( "ip", ip ).add( "mac", mac )
                      .toString();
    }
}
