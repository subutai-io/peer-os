package io.subutai.common.host;


import io.subutai.common.settings.Common;


/**
 * Null object for interface Interface
 */
public class NullHostInterface extends HostInterfaceModel
{
    private static HostInterfaceModel instance = new NullHostInterface();


    private NullHostInterface()
    {
    }


    public static HostInterfaceModel getInstance()
    {
        return instance;
    }


    @Override
    public String getName()
    {
        return Common.DEFAULT_CONTAINER_INTERFACE;
    }


    @Override
    public String getIp()
    {
        return Common.LOCAL_HOST_IP;
    }
}
