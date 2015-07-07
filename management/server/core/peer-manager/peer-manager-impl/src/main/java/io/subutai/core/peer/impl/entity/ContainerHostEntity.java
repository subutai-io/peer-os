package io.subutai.core.peer.impl.entity;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.common.quota.CpuQuotaInfo;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.RamQuota;
import io.subutai.core.hostregistry.api.ContainerHostInfo;
import io.subutai.core.peer.api.ContainerGroup;
import io.subutai.core.peer.api.ContainerGroupNotFoundException;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ResourceHost;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * ContainerHost class.
 */
@Entity
@Table( name = "container_host" )
@Access( AccessType.FIELD )
public class ContainerHostEntity extends AbstractSubutaiHost implements ContainerHost
{

    @ManyToOne( targetEntity = ResourceHostEntity.class )
    @JoinColumn( name = "parent_id" )
    private ResourceHost parent;


    @ElementCollection( targetClass = String.class, fetch = FetchType.EAGER )
    private Set<String> tags = new HashSet<>();

    @Transient
    private volatile ContainerHostState state = ContainerHostState.STOPPED;

    @Transient
    private DataService dataService;

    @Transient
    private LocalPeer localPeer;


    protected ContainerHostEntity()
    {
    }


    public void setLocalPeer( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    public void setDataService( final DataService dataService )
    {
        this.dataService = dataService;
    }


    public ContainerHostEntity( String peerId, HostInfo hostInfo )
    {
        super( peerId, hostInfo );
    }


    public String getEnvironmentId()
    {
        try
        {
            ContainerGroup containerGroup = localPeer.findContainerGroupByContainerId( getId() );

            return containerGroup.getEnvironmentId().toString();
        }
        catch ( ContainerGroupNotFoundException e )
        {
            return null;
        }
    }


    @Override
    public void addTag( final String tag )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tag ) );
        this.tags.add( tag );
        this.dataService.update( this );
    }


    @Override
    public void removeTag( final String tag )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tag ) );
        this.tags.remove( tag );
        this.dataService.update( this );
    }


    @Override
    public Set<String> getTags()
    {
        return this.tags;
    }


    @Override
    public void setDefaultGateway( final String gatewayIp ) throws PeerException
    {
        getPeer().setDefaultGateway( this, gatewayIp );
    }


    @Override
    public boolean isLocal()
    {
        return true;
    }


    public ContainerHostState getState()
    {
        return state;
    }


    public ResourceHost getParent()
    {
        return parent;
    }


    public void setParent( final ResourceHost parent )
    {
        this.parent = parent;
    }


    //unsupported START
    public String getNodeGroupName()
    {
        throw new UnsupportedOperationException();
    }


    public String getTemplateName()
    {
        throw new UnsupportedOperationException();
    }


    public Template getTemplate() throws PeerException
    {
        throw new UnsupportedOperationException();
    }
    //unsupported END


    public void dispose() throws PeerException
    {
        Peer peer = getPeer();
        peer.destroyContainer( this );
    }


    @Override
    public void start() throws PeerException
    {
        Peer peer = getPeer();
        peer.startContainer( this );
    }


    @Override
    public void stop() throws PeerException
    {
        Peer peer = getPeer();
        peer.stopContainer( this );
    }


    @Override
    public void updateHostInfo( final HostInfo hostInfo )
    {
        super.updateHostInfo( hostInfo );

        ContainerHostInfo containerHostInfo = ( ContainerHostInfo ) hostInfo;
        this.state = containerHostInfo.getStatus();
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final int processPid ) throws PeerException
    {
        Peer peer = getPeer();
        return peer.getProcessResourceUsage( this, processPid );
    }


    @Override
    public int getRamQuota() throws PeerException
    {
        return getPeer().getRamQuota( this );
    }


    @Override
    public RamQuota getRamQuotaInfo() throws PeerException
    {
        return getPeer().getRamQuotaInfo( this );
    }


    @Override
    public void setRamQuota( final int ramInMb ) throws PeerException
    {
        getPeer().setRamQuota( this, ramInMb );
    }


    @Override
    public void setRamQuota( final RamQuota ramQuota ) throws PeerException
    {
        getPeer().setRamQuota( this, ramQuota );
    }


    @Override
    public int getCpuQuota() throws PeerException
    {
        return getPeer().getCpuQuota( this );
    }


    @Override
    public CpuQuotaInfo getCpuQuotaInfo() throws PeerException
    {
        return getPeer().getCpuQuotaInfo( this );
    }


    @Override
    public void setCpuQuota( final int cpuPercent ) throws PeerException
    {
        getPeer().setCpuQuota( this, cpuPercent );
    }


    @Override
    public Set<Integer> getCpuSet() throws PeerException
    {
        return getPeer().getCpuSet( this );
    }


    @Override
    public void setCpuSet( final Set<Integer> cpuSet ) throws PeerException
    {
        getPeer().setCpuSet( this, cpuSet );
    }


    @Override
    public DiskQuota getDiskQuota( final DiskPartition diskPartition ) throws PeerException
    {
        return getPeer().getDiskQuota( this, diskPartition );
    }


    @Override
    public void setDiskQuota( final DiskQuota diskQuota ) throws PeerException
    {
        getPeer().setDiskQuota( this, diskQuota );
    }


    @Override
    public int getAvailableRamQuota() throws PeerException
    {
        return getPeer().getAvailableRamQuota( this );
    }


    @Override
    public int getAvailableCpuQuota() throws PeerException
    {
        return getPeer().getAvailableCpuQuota( this );
    }


    @Override
    public DiskQuota getAvailableDiskQuota( final DiskPartition diskPartition ) throws PeerException
    {
        return getPeer().getAvailableDiskQuota( this, diskPartition );
    }
}
