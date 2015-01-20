package org.safehaus.subutai.common.peer;


import org.safehaus.subutai.common.host.Interface;


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
