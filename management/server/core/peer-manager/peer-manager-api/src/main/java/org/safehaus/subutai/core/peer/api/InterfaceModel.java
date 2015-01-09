package org.safehaus.subutai.core.peer.api;


import org.safehaus.subutai.core.hostregistry.api.Interface;


/**
 * Created by timur on 11/30/14.
 */
public class InterfaceModel implements Interface
{
    private String interfaceName;
    private String ip;
    private String mac;


    public InterfaceModel( final Interface aInterface )
    {
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
}
