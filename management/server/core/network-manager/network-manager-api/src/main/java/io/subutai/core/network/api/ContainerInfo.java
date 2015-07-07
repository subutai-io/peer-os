package io.subutai.core.network.api;


/**
 * Network info of container
 */
public interface ContainerInfo
{
    public String getIp();

    public int getNetMask();

    public int getVLanId();
}
