package org.safehaus.subutai.core.environment.impl.net;


class NetworkSetupHelper
{

    public static final String DEFAULT_TUNNEL_TYPE = "vxlan";
    public static final String DEFAULT_INTERFACE_NAME = "eth1";


    private NetworkSetupHelper()
    {
    }


    public static String getTunnelType()
    {
        // TODO: always returns default now
        return DEFAULT_TUNNEL_TYPE;
    }


    public static String getInterfaceName()
    {
        // TODO: always returns default now
        return DEFAULT_INTERFACE_NAME;
    }
}

