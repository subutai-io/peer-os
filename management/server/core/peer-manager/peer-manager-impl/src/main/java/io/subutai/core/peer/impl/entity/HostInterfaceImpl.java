package io.subutai.core.peer.impl.entity;


import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import io.subutai.common.host.Interface;


@XmlRootElement
public class HostInterfaceImpl implements Interface, Serializable
{
    private String interfaceName;
    private String ip;
    private String mac;
    private AbstractSubutaiHost host;


    protected HostInterfaceImpl()
    {
    }


    public HostInterfaceImpl( final Interface s )
    {
        this.interfaceName = s.getInterfaceName();
        this.ip = s.getIp().replace( "addr:", "" );
        this.mac = s.getMac();
    }


    @Override
    public String getInterfaceName()
    {
        return interfaceName;
    }


    public void setInterfaceName( final String interfaceName )
    {
        this.interfaceName = interfaceName;
    }


    @Override
    public String getIp()
    {
        return ip;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    @Override
    public String getMac()
    {
        return mac;
    }


    public void setMac( final String mac )
    {
        this.mac = mac;
    }


    public AbstractSubutaiHost getHost()
    {
        return host;
    }


    public void setHost( final AbstractSubutaiHost host )
    {
        this.host = host;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof HostInterfaceImpl ) )
        {
            return false;
        }

        final HostInterfaceImpl that = ( HostInterfaceImpl ) o;

        if ( !ip.equals( that.ip ) )
        {
            return false;
        }
        return mac.equals( that.mac );
    }


    @Override
    public int hashCode()
    {
        int result = ip.hashCode();
        result = 31 * result + mac.hashCode();
        return result;
    }
}

