package org.safehaus.subutai.core.peer.impl.entity;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostState;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.HostKey;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
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

    @Column( name = "env_id", nullable = false )
    private String environmentId = "UNKNOWN";
    @Column( name = "creator_id", nullable = false )
    private String creatorPeerId = "UNKNOWN";
    @Column( name = "template_name", nullable = false )
    private String templateName = "UNKNOWN";
    @Column( name = "template_arch", nullable = false )
    private String templateArch = "UNKNOWN";

    @Transient
    private volatile ContainerHostState state = ContainerHostState.STOPPED;
    @Column( name = "node_group_name", nullable = false )
    private String nodeGroupName = "UNKNOWN";

    private QuotaManager quotaManager;

    @ElementCollection( targetClass = String.class, fetch = FetchType.EAGER )
    private Set<String> tags = new HashSet<>();
    @Transient
    private DataService dataService;


    private ContainerHostEntity()
    {
    }


    @Override
    public void setDataService( final DataService dataService )
    {
        this.dataService = dataService;
    }


    public ContainerHostEntity( String peerId, HostInfo hostInfo )
    {
        super( peerId, hostInfo );
        this.creatorPeerId = "UNKNOWN";
        this.environmentId = "UNKNOWN";
        this.nodeGroupName = "UNKNOWN";
        this.templateArch = "amd64";
        this.templateName = "UNKNOWN";
        //        this.parentHostname = parentHostname;
    }


    public ContainerHostEntity( final String peerId, final String creatorPeerId, final String environmentId,
                                final String nodeGroupName, final HostInfo hostInfo )
    {
        super( peerId, hostInfo );
        this.creatorPeerId = creatorPeerId;
        this.environmentId = environmentId;
        this.nodeGroupName = nodeGroupName;
        this.templateArch = "amd64";
        this.templateName = "UNKNOWN";
    }


    public ContainerHostEntity( final HostKey hostKey )
    {
        this.hostId = hostKey.getHostId();
        this.peerId = hostKey.getPeerId();
        this.creatorPeerId = hostKey.getCreatorId();
        this.environmentId = hostKey.getEnvironmentId();
        this.nodeGroupName = hostKey.getNodeGroupName();
    }


    public String getNodeGroupName()
    {
        return nodeGroupName;
    }


    public void setNodeGroupName( final String nodeGroupName )
    {
        this.nodeGroupName = nodeGroupName;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    public String getCreatorPeerId()
    {
        return creatorPeerId;
    }


    public void setCreatorPeerId( final String creatorPeerId )
    {
        this.creatorPeerId = creatorPeerId;
    }


    public String getTemplateName()
    {
        return templateName;
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


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    @Override
    public String getTemplateArch()
    {
        return templateArch;
    }


    @Override
    public void setTemplateArch( final String templateArch )
    {
        this.templateArch = templateArch;
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
        this.parent = ( ResourceHostEntity ) parent;
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


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final ContainerHost containerHost, final int processPid )
            throws PeerException
    {
        Peer peer = getPeer();
        return peer.getProcessResourceUsage( containerHost, processPid );
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
    public void updateHostInfo( final HostInfo hostInfo )
    {
        super.updateHostInfo( hostInfo );

        ContainerHostInfo conatinerHostInfo = ( ContainerHostInfo ) hostInfo;
        this.state = conatinerHostInfo.getStatus();
    }


    @Override
    public String getParentHostname()
    {
        return parent.getHostname();
    }


    @Override
    public int getRamQuota( final UUID containerId ) throws PeerException
    {
        return getPeer().getRamQuota( containerId );
    }


    @Override
    public void setRamQuota( final UUID containerId, final int ramInMb ) throws PeerException
    {
        getPeer().setRamQuota( containerId, ramInMb );
    }


    @Override
    public int getCpuQuota( final UUID containerId ) throws PeerException
    {
        return getPeer().getCpuQuota( containerId );
    }


    @Override
    public void setCpuQuota( final UUID containerId, final int cpuPercent ) throws PeerException
    {
        getPeer().setCpuQuota( containerId, cpuPercent );
    }


    @Override
    public Set<Integer> getCpuSet( final UUID containerId ) throws PeerException
    {
        return getPeer().getCpuSet( containerId );
    }


    @Override
    public void setCpuSet( final UUID containerId, final Set<Integer> cpuSet ) throws PeerException
    {
        getPeer().setCpuSet( containerId, cpuSet );
    }


    @Override
    public DiskQuota getDiskQuota( final UUID containerId, final DiskPartition diskPartition ) throws PeerException
    {
        return getPeer().getDiskQuota( containerId, diskPartition );
    }


    @Override
    public void setDiskQuota( final UUID containerId, final DiskQuota diskQuota ) throws PeerException
    {
        getPeer().setDiskQuota( containerId, diskQuota );
    }
}
