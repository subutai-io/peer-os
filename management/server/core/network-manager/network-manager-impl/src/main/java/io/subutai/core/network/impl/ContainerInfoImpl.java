package io.subutai.core.network.impl;


import io.subutai.core.network.api.ContainerInfo;


/**
 * ContainerInfo implementation
 */
public class ContainerInfoImpl implements ContainerInfo
{
    private final String ip;
    private final int netMask;
    private final int vLanId;


    public ContainerInfoImpl( final String ip, final int netMask, final int vLanId )
    {
        this.ip = ip;
        this.netMask = netMask;
        this.vLanId = vLanId;
    }


    @Override
    public String getIp()
    {
        return ip;
    }


    @Override
    public int getNetMask()
    {
        return netMask;
    }


    public int getVLanId()
    {
        return vLanId;
    }
}
