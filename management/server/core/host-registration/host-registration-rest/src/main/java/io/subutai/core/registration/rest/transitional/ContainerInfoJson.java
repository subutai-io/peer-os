package io.subutai.core.registration.rest.transitional;


import java.util.HashSet;
import java.util.Set;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.core.registration.api.ResourceHostRegistrationStatus;
import io.subutai.core.registration.api.service.ContainerInfo;


public class ContainerInfoJson implements ContainerInfo
{
    private String id;
    private String hostname;
    private String containerName;
    private Integer vlan;
    private String templateName;
    private Set<HostInterfaceJson> interfaces = new HashSet<>();
    private HostArchitecture arch;
    private String publicKey;
    private String gateway;
    private ResourceHostRegistrationStatus status = ResourceHostRegistrationStatus.REQUESTED;
    private ContainerHostState state;


    public ContainerInfoJson()
    {
        arch = HostArchitecture.AMD64;
    }


    public ContainerInfoJson( ContainerInfo hostInfo )
    {
        this.id = hostInfo.getId();
        this.hostname = hostInfo.getHostname();
        this.containerName = hostInfo.getContainerName();
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
            this.interfaces.add( new HostInterfaceJson( anHostInterface ) );
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
    public String getContainerName()
    {
        return containerName;
    }


    @Override
    public String getGateway()
    {
        return gateway;
    }



    @Override
    public ContainerHostState getState()
    {
        return state;
    }


    @Override
    public HostInterfaces getHostInterfaces()
    {
        HostInterfaces result = new HostInterfaces();
        if ( this.interfaces == null )
        {
            return result;
        }
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



    @Override
    public Integer getVlan()
    {
        return vlan;
    }




    @Override
    public String getPublicKey()
    {
        return publicKey;
    }




    @Override
    public ResourceHostRegistrationStatus getStatus()
    {
        return status;
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
