package io.subutai.core.security.broker;


import com.google.common.base.Objects;

import io.subutai.common.host.Interface;


/**
 * Network interface
 */
public class InterfaceImpl implements Interface
{

    private String interfaceName;
    private String ip;
    private String mac;


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
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "interfaceName", interfaceName ).add( "ip", ip ).add( "mac", mac )
                      .toString();
    }
}
