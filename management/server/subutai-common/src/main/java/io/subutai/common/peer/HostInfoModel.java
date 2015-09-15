package io.subutai.common.peer;


import java.util.HashSet;
import java.util.Set;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;


public class HostInfoModel implements HostInfo
{
    private String id;
    private String hostname;
    private Set<InterfaceModel> netInterfaces = new HashSet<>();
    private HostArchitecture hostArchitecture;


    public HostInfoModel( HostInfo hostInfo )
    {
        this.id = hostInfo.getId();
        this.hostname = hostInfo.getHostname();
        this.hostArchitecture = hostInfo.getArch();
        if ( hostArchitecture == null )
        {
            hostArchitecture = HostArchitecture.AMD64;
        }
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
        if ( hostArchitecture == null )
        {
            hostArchitecture = HostArchitecture.AMD64;
        }
        for ( Interface anInterface : containerHost.getNetInterfaces() )
        {
            this.netInterfaces.add( new InterfaceModel( anInterface ) );
        }
    }


    @Override
    public String getId()
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


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof HostInfoModel ) )
        {
            return false;
        }

        final HostInfoModel that = ( HostInfoModel ) o;

        return hostArchitecture == that.hostArchitecture && hostname.equals( that.hostname ) && id.equals( that.id )
                && netInterfaces.equals( that.netInterfaces );
    }


    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + hostname.hashCode();
        result = 31 * result + netInterfaces.hashCode();
        result = 31 * result + hostArchitecture.hashCode();
        return result;
    }
}
