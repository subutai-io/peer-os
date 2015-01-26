package org.safehaus.subutai.core.environment.ui.manage;


import org.safehaus.subutai.core.network.api.N2NConnection;


class N2NConnectionImpl implements N2NConnection
{
    String superNodeIp;
    int superNodePort;
    String localIp;
    String interfaceName;
    String communityName;


    public static N2NConnectionImpl newCopy( N2NConnection n2n )
    {
        N2NConnectionImpl n = new N2NConnectionImpl();
        if ( n2n != null )
        {
            n.superNodeIp = n2n.getSuperNodeIp();
            n.superNodePort = n2n.getSuperNodePort();
            n.localIp = n2n.getLocalIp();
            n.interfaceName = n2n.getInterfaceName();
            n.communityName = n2n.getCommunityName();
        }
        return n;
    }


    @Override
    public String getSuperNodeIp()
    {
        return superNodeIp;
    }


    @Override
    public int getSuperNodePort()
    {
        return superNodePort;
    }


    @Override
    public String getLocalIp()
    {
        return localIp;
    }


    @Override
    public String getInterfaceName()
    {
        return interfaceName;
    }


    @Override
    public String getCommunityName()
    {
        return communityName;
    }


    public boolean hasAllValues()
    {
        return superNodeIp != null && !superNodeIp.isEmpty() && superNodePort > 0 && localIp != null && !localIp
                .isEmpty() && interfaceName != null && !interfaceName.isEmpty() && communityName != null
                && !communityName.isEmpty();
    }
}

