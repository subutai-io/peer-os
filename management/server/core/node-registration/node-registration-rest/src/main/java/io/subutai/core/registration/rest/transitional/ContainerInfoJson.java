package io.subutai.core.registration.rest.transitional;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;
import io.subutai.core.registration.api.RegistrationStatus;
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
    private Set<HostInterfaceJson> interfaces = new HashSet<>();
    private HostArchitecture arch;
    private String publicKey;
    private String gateway;
    private RegistrationStatus status = RegistrationStatus.REQUESTED;


    public ContainerInfoJson()
    {
        arch = HostArchitecture.AMD64;
    }


    public ContainerInfoJson( ContainerInfo hostInfo )
    {
        this.id = hostInfo.getId().toString();
        this.hostname = hostInfo.getHostname();
        this.templateName = hostInfo.getTemplateName();
        this.vlan = hostInfo.getVlan();
        this.arch = hostInfo.getArch();
        this.publicKey = hostInfo.getPublicKey();
        this.status = hostInfo.getStatus();
        this.gateway = hostInfo.getGateway();
        if ( arch == null )
        {
            arch = HostArchitecture.AMD64;
        }
        for ( Interface anInterface : hostInfo.getInterfaces() )
        {
            this.interfaces.add( new HostInterfaceJson( anInterface ) );
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
    public String getGateway()
    {
        return gateway;
    }


    public void setGateway( final String gateway )
    {
        this.gateway = gateway;
    }


    @Override
    public Set<Interface> getInterfaces()
    {
        Set<Interface> result = Sets.newHashSet();
        result.addAll( this.interfaces );
        return result;
    }


    @Override
    public HostArchitecture getArch()
    {
        return arch;
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
    public String getPublicKey()
    {
        return publicKey;
    }


    public void setPublicKey( final String publicKey )
    {
        this.publicKey = publicKey;
    }


    @Override
    public RegistrationStatus getStatus()
    {
        return status;
    }


    public void setStatus( final RegistrationStatus status )
    {
        this.status = status;
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
        if ( !( o instanceof ContainerInfoJson ) )
        {
            return false;
        }

        final ContainerInfoJson that = ( ContainerInfoJson ) o;

        return arch == that.arch && hostname.equals( that.hostname ) && id.equals( that.id ) && interfaces
                .equals( that.interfaces );
    }


    @Override
    public int hashCode()
    {
        return id.hashCode();
    }


    @Override
    public String toString()
    {
        return "ContainerHostInfoModel{" +
                "id='" + id + '\'' +
                ", hostname='" + hostname + '\'' +
                ", interfaces=" + interfaces +
                ", arch=" + arch +
                '}';
    }
}
