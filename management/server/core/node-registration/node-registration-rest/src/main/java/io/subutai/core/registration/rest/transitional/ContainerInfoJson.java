package io.subutai.core.registration.rest.transitional;


import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
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
    private Set<HostHostInterfaceJson> interfaces = new HashSet<>();
    private HostArchitecture arch;
    private String publicKey;
    private String gateway;
    private RegistrationStatus status = RegistrationStatus.REQUESTED;
    private ContainerHostState state;


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
        this.state = hostInfo.getState();
        if ( arch == null )
        {
            arch = HostArchitecture.AMD64;
        }
        for ( HostInterface anHostInterface : hostInfo.getHostInterfaces().getAll() )
        {
            this.interfaces.add( new HostHostInterfaceJson( anHostInterface ) );
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
    public String getGateway()
    {
        return gateway;
    }


    public void setGateway( final String gateway )
    {
        this.gateway = gateway;
    }


    @Override
    public ContainerHostState getState()
    {
        return state;
    }


    @Override
    public String getContainerName()
    {
        return null;
    }


    @Override
    public HostInterfaces getHostInterfaces()
    {
        HostInterfaces result = new HostInterfaces();
        for ( HostInterface hostInterface : this.interfaces )
        {
            HostInterfaceModel model = new HostInterfaceModel( hostInterface );
            result.addHostInterface( model );
        }
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
