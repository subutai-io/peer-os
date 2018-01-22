package io.subutai.common.peer;


import io.subutai.common.host.ContainerHostState;


public class ContainerInfo
{
    private String name;
    private String ip;
    private String iface;
    private ContainerHostState state;


    public ContainerInfo( final String name, final String ip, final String iface, final ContainerHostState state )
    {
        this.name = name;
        this.ip = ip;
        this.iface = iface;
        this.state = state;
    }


    public String getName()
    {
        return name;
    }


    public String getIp()
    {
        return ip;
    }


    public String getIface()
    {
        return iface;
    }


    public ContainerHostState getState()
    {
        return state;
    }


    @Override
    public String toString()
    {
        return "ContainerInfo{" + "name='" + name + '\'' + ", ip='" + ip + '\'' + ", iface='" + iface + '\''
                + ", state=" + state + '}';
    }
}
