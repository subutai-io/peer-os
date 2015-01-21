package org.safehaus.subutai.core.env.impl.entity;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.SubutaiException;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostArchitecture;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.host.Interface;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.HostEvent;
import org.safehaus.subutai.common.peer.HostEventListener;
import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.env.api.Environment;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


@Entity
@Table( name = "environment_container" )
@Access( AccessType.FIELD )
public class EnvironmentContainerImpl implements ContainerHost, Serializable
{
    @Column( name = "peer_id" )
    private String peerId;
    @Column( name = "host_id" )
    private String hostId;
    @Column( name = "hostname" )
    private String hostname;
    @Column( name = "node_group_name" )
    private String nodeGroupName;
    @Column( name = "creator_peer_id" )
    private String creatorPeerId;
    @Column( name = "template_name" )
    private String templateName;
    @Column( name = "template_arch" )
    private String templateArch;

    @ElementCollection( targetClass = String.class, fetch = FetchType.EAGER )
    private Set<String> tags = new HashSet<>();

    @ManyToOne( targetEntity = EnvironmentImpl.class )
    @JoinColumn( name = "environment_id" )
    private Environment environment;

    @Column( name = "arch" )
    @Enumerated
    private HostArchitecture hostArchitecture;

    @OneToMany( mappedBy = "host", fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = HostInterface
            .class, orphanRemoval = true )
    protected Set<Interface> interfaces = new HashSet<>();


    @Transient
    private Peer peer;
    @Transient
    private DataService dataService;


    private EnvironmentContainerImpl()
    {
    }


    @Override
    public void init()
    {
        // Empty method
    }


    public EnvironmentContainerImpl( final Peer peer, final String nodeGroupName, final HostInfoModel hostInfo,
                                     final Template template )
    {
        Preconditions.checkNotNull( peer );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( nodeGroupName ) );
        Preconditions.checkNotNull( hostInfo );
        Preconditions.checkNotNull( template );


        this.peer = peer;
        this.peerId = peer.getId().toString();
        this.hostId = hostInfo.getId().toString();
        this.hostname = hostInfo.getHostname();
        this.hostArchitecture = hostInfo.getArch();
        this.nodeGroupName = nodeGroupName;
        this.templateName = template.getTemplateName();
        this.templateArch = template.getLxcArch();
        for ( Interface anInterface : hostInfo.getInterfaces() )
        {
            HostInterface hostInterface = new HostInterface( anInterface );
            hostInterface.setHost( this );
            this.interfaces.add( hostInterface );
        }
    }


    @Override
    public void setDataService( final DataService dataService )
    {
        this.dataService = dataService;
    }


    @Override
    public String getEnvironmentId()
    {
        return environment.getId().toString();
    }


    @Override
    public void setNodeGroupName( final String nodeGroupName )
    {
        this.nodeGroupName = nodeGroupName;
    }


    @Override
    public void setCreatorPeerId( final String creatorPeerId )
    {
        this.creatorPeerId = creatorPeerId;
    }


    @Override
    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    @Override
    public String getNodeGroupName()
    {
        return this.nodeGroupName;
    }


    @Override
    public String getTemplateArch()
    {
        return this.templateArch;
    }


    @Override
    public void setTemplateArch( final String templateArch )
    {
        this.templateArch = templateArch;
    }


    @Override
    public ContainerHostState getState() throws PeerException
    {
        return getPeer().getContainerHostState( hostId );
    }


    @Override
    public String getCreatorPeerId()
    {
        return creatorPeerId;
    }


    @Override
    public void dispose() throws PeerException
    {
        getPeer().destroyContainer( this );
    }


    @Override
    public Peer getPeer()
    {
        return this.peer;
    }


    @Override
    public Template getTemplate() throws PeerException
    {
        return getPeer().getTemplate( this.templateName );
    }


    @Override
    public String getTemplateName()
    {
        return this.templateName;
    }


    @Override
    public void addTag( final String tag )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tag ) );
        this.tags.add( tag );
        dataService.update( this );
    }


    @Override
    public void removeTag( final String tag )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tag ) );
        this.tags.remove( tag );
        dataService.update( this );
    }


    @Override
    public Set<String> getTags()
    {
        return this.tags;
    }


    @Override
    public String getPeerId()
    {
        return this.peerId;
    }


    @Override
    public UUID getId()
    {
        return UUID.fromString( hostId );
    }


    @Override
    public String getHostId()
    {
        return this.hostId;
    }


    @Override
    public String getHostname()
    {
        return this.hostname;
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder ) throws CommandException
    {
        return getPeer().execute( requestBuilder, this );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final CommandCallback callback )
            throws CommandException
    {
        return getPeer().execute( requestBuilder, this, callback );
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final CommandCallback callback )
            throws CommandException
    {
        getPeer().executeAsync( requestBuilder, this, callback );
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder ) throws CommandException
    {
        getPeer().executeAsync( requestBuilder, this );
    }


    @Override
    public boolean isConnected()
    {
        return getPeer().isConnected( this );
    }


    @Override
    public String getIpByMask( final String mask )
    {
        for ( Iterator<Interface> iterator = interfaces.iterator(); iterator.hasNext(); )
        {
            Interface intf = iterator.next();
            if ( intf.getIp().matches( mask ) )
            {
                return intf.getIp();
            }
        }
        return null;
    }


    @Override
    public void addIpHostToEtcHosts( final String domainName, final Set<Host> others, final String mask )
            throws SubutaiException
    {
        StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
        StringBuilder appendHosts = new StringBuilder();
        for ( Host otherHost : others )
        {
            if ( getId().equals( otherHost.getId() ) )
            {
                continue;
            }

            String ip = otherHost.getIpByMask( Common.IP_MASK );
            String hostname = otherHost.getHostname();
            cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
            appendHosts.append( "/bin/echo '" ).
                    append( ip ).append( " " ).
                               append( hostname ).append( "." ).append( domainName ).
                               append( " " ).append( hostname ).
                               append( "' >> '/etc/hosts'; " );
        }
        if ( cleanHosts.length() > 0 )
        {
            //drop pipe | symbol
            cleanHosts.setLength( cleanHosts.length() - 1 );
            cleanHosts.insert( 0, "egrep -v '" );
            cleanHosts.append( "' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;" );
            appendHosts.insert( 0, cleanHosts );
        }

        appendHosts.append( "/bin/echo '127.0.0.1 localhost " ).append( getHostname() ).append( "' >> '/etc/hosts';" );

        try
        {
            execute( new RequestBuilder( appendHosts.toString() ).withTimeout( 30 ) );
        }
        catch ( CommandException e )
        {
            throw new SubutaiException( "Could not add to /etc/hosts: " + e.toString() );
        }
    }


    private List<String> getIps() throws PeerException
    {
        List<String> result = new ArrayList<>();

        for ( Interface anInterface : interfaces )
        {
            String ip = anInterface.getIp();
            result.add( ip );
        }
        return result;
    }


    @Override
    public Set<Interface> getNetInterfaces()
    {
        return interfaces;
    }


    @Override
    public String getIpByInterfaceName( String interfaceName )
    {
        for ( Interface iface : interfaces )
        {
            if ( iface.getInterfaceName().equalsIgnoreCase( interfaceName ) )
            {
                return iface.getIp();
            }
        }

        return null;
    }


    @Override
    public String getMacByInterfaceName( final String interfaceName )
    {
        for ( Interface iface : interfaces )
        {
            if ( iface.getInterfaceName().equalsIgnoreCase( interfaceName ) )
            {
                return iface.getMac();
            }
        }

        return null;
    }


    public void setEnvironment( Environment environment )
    {
        this.environment = environment;
    }


    @Override
    public HostArchitecture getHostArchitecture()
    {
        return this.hostArchitecture;
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final int processPid ) throws PeerException
    {
        return getPeer().getProcessResourceUsage( getId(), processPid );
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


    //unsupported
    @Override
    public void setPeer( final Peer peer )
    {
        throw new UnsupportedOperationException( "Unsupported operation." );
    }


    @Override
    public long getLastHeartbeat()
    {
        throw new UnsupportedOperationException( "Unsupported operation." );
    }


    @Override
    public void updateHostInfo( final HostInfo hostInfo )
    {
        throw new UnsupportedOperationException( "Unsupported operation." );
    }


    @Override
    public void addListener( final HostEventListener hostEventListener )
    {
        throw new UnsupportedOperationException( "Unsupported operation." );
    }


    @Override
    public void removeListener( final HostEventListener hostEventListener )
    {
        throw new UnsupportedOperationException( "Unsupported operation." );
    }


    @Override
    public void fireEvent( final HostEvent hostEvent )
    {
        throw new UnsupportedOperationException( "Unsupported operation." );
    }


    public PeerQuotaInfo getQuota( QuotaType quotaType ) throws PeerException
    {
        throw new UnsupportedOperationException( "Unsupported operation." );
    }


    public void setQuota( QuotaInfo quota ) throws PeerException
    {
        throw new UnsupportedOperationException( "Unsupported operation." );
    }


    @Override
    public void setEnvironmentId( final String environmentId )
    {
        throw new UnsupportedOperationException( "Unsupported operation." );
    }


    @Override
    public String getParentHostname()
    {
        throw new UnsupportedOperationException( "Unsupported method." );
    }
}
