package org.safehaus.subutai.core.peer.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.hostregistry.api.HostArchitecture;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.Interface;


/**
 * Created by timur on 11/30/14.
 */
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
