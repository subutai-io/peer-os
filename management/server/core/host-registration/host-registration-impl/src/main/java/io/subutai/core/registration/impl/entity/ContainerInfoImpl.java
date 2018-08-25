package io.subutai.core.registration.impl.entity;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.Quota;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.registration.api.ResourceHostRegistrationStatus;
import io.subutai.core.registration.api.service.ContainerInfo;


@Entity
@Access( AccessType.FIELD )
@Table( name = "node_container_host_model" )
public class ContainerInfoImpl implements ContainerInfo
{
    @Id
    @Column( name = "id" )
    @Expose
    private String id;

    @Lob
    @Column
    @Expose
    private String publicKey;

    @Column( name = "host_name" )
    @Expose
    private String hostname;

    @Column( name = "container_name" )
    @Expose
    private String containerName;

    @Column( name = "vlan" )
    @Expose
    private Integer vlan;

    @Column( name = "envId" )
    @Expose
    private String envId;

    @Column( name = "gateway" )
    @Expose
    private String gateway = "";

    @Column( name = "template_name" )
    @Expose
    private String templateName;


    @JoinColumn( name = "net_interfaces" )
    @OneToMany( orphanRemoval = true, targetEntity = HostInterfaceImpl.class, cascade = CascadeType.ALL, fetch =
            FetchType.EAGER )
    @Expose
    private Set<HostInterface> netHostInterfaces = new HashSet<>();

    @Column( name = "arch" )
    @Enumerated( EnumType.STRING )
    @Expose
    private HostArchitecture arch = HostArchitecture.AMD64;

    @ManyToOne
    @JoinColumn( name = "requested_host" )
    private RequestedHostImpl requestedHost;

    @Column( name = "reg_status" )
    @Enumerated( EnumType.STRING )
    @Expose
    private ResourceHostRegistrationStatus status = ResourceHostRegistrationStatus.REQUESTED;

    @Column( name = "state" )
    @Enumerated( EnumType.STRING )
    @Expose
    private ContainerHostState state;


    public ContainerInfoImpl()
    {
    }


    public ContainerInfoImpl( ContainerInfo hostInfo )
    {
        this.id = hostInfo.getId();
        this.hostname = hostInfo.getHostname();
        this.containerName = hostInfo.getContainerName();
        this.templateName = hostInfo.getTemplateName();
        this.state = hostInfo.getState();
        this.vlan = hostInfo.getVlan();
        this.arch = hostInfo.getArch();
        this.status = hostInfo.getStatus();
        this.publicKey = hostInfo.getPublicKey();
        this.gateway = hostInfo.getGateway();
        this.envId = hostInfo.getEnvId();
        if ( arch == null )
        {
            arch = HostArchitecture.AMD64;
        }
        for ( HostInterface anHostInterface : hostInfo.getHostInterfaces().getAll() )
        {
            this.netHostInterfaces.add( new HostInterfaceImpl( anHostInterface ) );
        }
    }


    @Override
    public Quota getRawQuota()
    {
        try
        {
            return getLocalPeer().getRawQuota( new ContainerId( id ) );
        }
        catch ( Exception e )
        {
            return null;
        }
    }


    protected LocalPeer getLocalPeer()
    {
        return ServiceLocator.lookup( LocalPeer.class );
    }


    @Override
    public ContainerHostState getState()
    {
        return state;
    }


    @Override
    public ResourceHostRegistrationStatus getStatus()
    {
        return status;
    }


    public void setStatus( final ResourceHostRegistrationStatus status )
    {
        this.status = status;
    }


    public void setRequestedHost( final RequestedHostImpl requestedHost )
    {
        this.requestedHost = requestedHost;
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
    public HostInterfaces getHostInterfaces()
    {
        HostInterfaces result = new HostInterfaces();
        for ( HostInterface hostInterface : this.netHostInterfaces )
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
    public String getPublicKey()
    {
        return publicKey;
    }


    @Override
    public Integer getVlan()
    {
        return vlan;
    }


    @Override
    public String getEnvId()
    {
        return envId;
    }


    @Override
    public String getGateway()
    {
        return gateway;
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
        if ( !( o instanceof ContainerInfoImpl ) )
        {
            return false;
        }

        final ContainerInfoImpl that = ( ContainerInfoImpl ) o;

        return arch == that.arch && hostname.equals( that.hostname ) && id.equals( that.id ) && netHostInterfaces
                .equals( that.netHostInterfaces );
    }


    @Override
    public int hashCode()
    {
        return id.hashCode();
    }


    @Override
    public String toString()
    {
        return "ContainerInfoImpl{" + "id='" + id + '\'' + ", publicKey='" + publicKey + '\'' + ", hostname='"
                + hostname + '\'' + ", vlan=" + vlan + ", templateName='" + templateName + '\'' + ", hostInterfaces="
                + netHostInterfaces + ", arch=" + arch + '}';
    }
}
