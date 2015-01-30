package org.safehaus.subutai.common.peer;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.host.HostArchitecture;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.host.Interface;


public class HostInfoModel implements HostInfo
{
    private UUID id;
    private String hostname;
    private Set<InterfaceModel> netInterfaces = new HashSet<>();
    private HostArchitecture hostArchitecture;


    public HostInfoModel( HostInfo hostInfo )
    {
        this.id = hostInfo.getId();
        this.hostname = hostInfo.getHostname();
        this.hostArchitecture = hostInfo.getArch();
        for ( Interface anInterface : hostInfo.getInterfaces() )
        {
            this.netInterfaces.add( new InterfaceModel( anInterface ) );
        }
    }


    public HostInfoModel( final ContainerHost containerHost )
    {
        this.id = containerHost.getId();
        this.hostname = containerHost.getHostname();
        this.hostArchitecture = containerHost.getHostArchitecture();

        for ( Interface anInterface : containerHost.getNetInterfaces() )
        {
            this.netInterfaces.add( new InterfaceModel( anInterface ) );
        }
    }


    @Override
    public UUID getId()
    {
        return id;
    }


    @Override
    public String getHostname()
    {
        return hostname;
    }


    @Override
    public Set<Interface> getInterfaces()
    {
        Set<Interface> result = new HashSet<>();
        result.addAll( netInterfaces );
        return result;
    }


    @Override
    public HostArchitecture getArch()
    {
        return hostArchitecture;
    }


    @Override
    public int compareTo( final HostInfo o )
    {
        return hostname.compareTo( o.getHostname() );
    }
}
