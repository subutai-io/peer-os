package io.subutai.core.registration.rest.transitional;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.Interface;
import io.subutai.common.peer.InterfaceModel;
import io.subutai.core.registration.api.service.ContainerInfo;


/**
 * Created by talas on 9/15/15.
 */
public class ContainerInfoJson implements ContainerInfo
{
    private String id;
    private String hostname;
    private Integer vlan;
    private String templateName;
    private Set<InterfaceModel> netInterfaces = new HashSet<>();
    private HostArchitecture hostArchitecture;


    public ContainerInfoJson()
    {
    }


    public ContainerInfoJson( ContainerInfo hostInfo )
    {
        this.id = hostInfo.getId().toString();
        this.hostname = hostInfo.getHostname();
        this.templateName = hostInfo.getTemplateName();
        this.vlan = hostInfo.getVlan();
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


    @Override
    public UUID getId()
    {
        return UUID.fromString( id );
    }


    @Override
    public String getHostname()
    {
        return hostname;
    }


    @Override
    public Set<Interface> getInterfaces()
    {
        Set<Interface> result = Sets.newHashSet();
        result.addAll( this.netInterfaces );
        return result;
    }


    @Override
    public HostArchitecture getArch()
    {
        return hostArchitecture;
    }


    @Override
    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    @Override
    public Integer getVlan()
    {
        return vlan;
    }


    public void setVlan( final Integer vlan )
    {
        this.vlan = vlan;
    }


    @Override
    public int compareTo( final ContainerInfo o )
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
        if ( !( o instanceof ContainerInfoJson ) )
        {
            return false;
        }

        final ContainerInfoJson that = ( ContainerInfoJson ) o;

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


    @Override
    public String toString()
    {
        return "ContainerHostInfoModel{" +
                "id='" + id + '\'' +
                ", hostname='" + hostname + '\'' +
                ", netInterfaces=" + netInterfaces +
                ", hostArchitecture=" + hostArchitecture +
                '}';
    }
}
