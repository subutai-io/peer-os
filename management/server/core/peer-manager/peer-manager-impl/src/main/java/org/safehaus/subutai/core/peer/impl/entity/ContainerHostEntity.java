package org.safehaus.subutai.core.peer.impl.entity;


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
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.peer.api.ResourceHost;

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


    protected ContainerHostEntity()
    {
    }


    public void setDataService( final DataService dataService )
    {
        this.dataService = dataService;
    }


    public ContainerHostEntity( String peerId, HostInfo hostInfo )
    {
        super( peerId, hostInfo );
    }


    public String getNodeGroupName()
    {
        throw new UnsupportedOperationException();
    }


    public String getEnvironmentId()
    {
        throw new UnsupportedOperationException();
    }


    public String getTemplateName()
    {
        throw new UnsupportedOperationException();
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


    public void setQuota( QuotaInfo quota ) throws PeerException
    {
        Peer peer = getPeer();
        peer.setQuota( this, quota );
    }


    public PeerQuotaInfo getQuota( final QuotaType quotaType ) throws PeerException
    {
        Peer peer = getPeer();
        return peer.getQuota( this, quotaType );
    }


    public Template getTemplate() throws PeerException
    {
        Peer peer = getPeer();
        return peer.getTemplate( getTemplateName() );
    }


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

        ContainerHostInfo conatinerHostInfo = ( ContainerHostInfo ) hostInfo;
        this.state = conatinerHostInfo.getStatus();
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final int processPid ) throws PeerException
    {
        Peer peer = getPeer();
        return peer.getProcessResourceUsage( getId(), processPid );
    }


    @Override
    public int getRamQuota() throws PeerException
    {
        return getPeer().getRamQuota( getId() );
    }


    @Override
    public void setRamQuota( final int ramInMb ) throws PeerException
    {
        getPeer().setRamQuota( getId(), ramInMb );
    }


    @Override
    public int getCpuQuota() throws PeerException
    {
        return getPeer().getCpuQuota( getId() );
    }


    @Override
    public void setCpuQuota( final int cpuPercent ) throws PeerException
    {
        getPeer().setCpuQuota( getId(), cpuPercent );
    }


    @Override
    public Set<Integer> getCpuSet() throws PeerException
    {
        return getPeer().getCpuSet( getId() );
    }


    @Override
    public void setCpuSet( final Set<Integer> cpuSet ) throws PeerException
    {
        getPeer().setCpuSet( getId(), cpuSet );
    }


    @Override
    public DiskQuota getDiskQuota( final DiskPartition diskPartition ) throws PeerException
    {
        return getPeer().getDiskQuota( getId(), diskPartition );
    }


    @Override
    public void setDiskQuota( final DiskQuota diskQuota ) throws PeerException
    {
        getPeer().setDiskQuota( getId(), diskQuota );
    }
}
