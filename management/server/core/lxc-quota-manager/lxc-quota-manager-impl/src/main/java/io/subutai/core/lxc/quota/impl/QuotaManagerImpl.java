package io.subutai.core.lxc.quota.impl;


import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.parser.CommonResourceValueParser;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.ContainerResourceFactory;
import io.subutai.hub.share.quota.Quota;
import io.subutai.hub.share.quota.QuotaException;
import io.subutai.hub.share.resource.ContainerResourceType;
import io.subutai.hub.share.resource.PeerResources;
import io.subutai.hub.share.resource.ResourceValueParser;


public class QuotaManagerImpl implements QuotaManager
{
    private static Logger LOGGER = LoggerFactory.getLogger( QuotaManagerImpl.class );
    private LocalPeer localPeer;
    private PeerManager peerManager;
    private CommandUtil commandUtil;
    protected Commands commands = new Commands();
    private EnumMap<ContainerSize, ContainerQuota> containerQuotas = new EnumMap<>( ContainerSize.class );
    private String defaultQuota;
    private ObjectMapper mapper = new ObjectMapper();


    public QuotaManagerImpl( PeerManager peerManager, LocalPeer localPeer )
    {
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( localPeer );
        this.peerManager = peerManager;
        this.localPeer = localPeer;
        this.commandUtil = new CommandUtil();
    }


    public void init() throws QuotaException
    {
        initDefaultQuotas();
    }


    protected void initDefaultQuotas() throws QuotaException
    {
        LOGGER.info( "Parsing default quota settings..." );
        String[] settings = defaultQuota.split( ":" );
        if ( settings.length != 5 )
        {
            throw new QuotaException( "Invalid default quota settings." );
        }

        int i = 0;
        for ( ContainerSize containerSize : ContainerSize.values() )
        {
            LOGGER.debug( String.format( "Settings for %s: %s", containerSize, settings[i] ) );
            String[] quotas = settings[i++].split( "\\|" );

            if ( quotas.length != 6 )
            {
                throw new QuotaException( String.format( "Invalid quota settings for %s.", containerSize ) );
            }

            try
            {
                final ContainerQuota quota = new ContainerQuota();

                ResourceValueParser quotaParser = getResourceValueParser( ContainerResourceType.RAM );
                quota.add( new Quota( ContainerResourceFactory
                        .createContainerResource( ContainerResourceType.RAM, quotaParser.parse( quotas[0] ) ), 0 ) );

                quotaParser = getResourceValueParser( ContainerResourceType.CPU );
                quota.add( new Quota( ContainerResourceFactory
                        .createContainerResource( ContainerResourceType.CPU, quotaParser.parse( quotas[1] ) ), 0 ) );

                quotaParser = getResourceValueParser( ContainerResourceType.OPT );
                quota.add( new Quota( ContainerResourceFactory
                        .createContainerResource( ContainerResourceType.OPT, quotaParser.parse( quotas[2] ) ), 0 ) );

                quotaParser = getResourceValueParser( ContainerResourceType.HOME );
                quota.add( new Quota( ContainerResourceFactory
                        .createContainerResource( ContainerResourceType.HOME, quotaParser.parse( quotas[3] ) ), 0 ) );

                quotaParser = getResourceValueParser( ContainerResourceType.VAR );
                quota.add( new Quota( ContainerResourceFactory
                        .createContainerResource( ContainerResourceType.VAR, quotaParser.parse( quotas[4] ) ), 0 ) );

                quotaParser = getResourceValueParser( ContainerResourceType.ROOTFS );
                quota.add( new Quota( ContainerResourceFactory
                        .createContainerResource( ContainerResourceType.ROOTFS, quotaParser.parse( quotas[5] ) ), 0 ) );

                containerQuotas.put( containerSize, quota );

                LOGGER.debug( quota.toString() );
            }
            catch ( Exception e )
            {
                throw new QuotaException( String.format( "Could not parse quota settings for %s.", containerSize ) );
            }
        }
        LOGGER.info( "Quota settings parsed." );
    }


    @Override
    public ResourceValueParser getResourceValueParser( final ContainerResourceType containerResourceType )
            throws QuotaException
    {
        return CommonResourceValueParser.getInstance( containerResourceType );
    }


    public void setDefaultQuota( final String defaultQuota )
    {
        this.defaultQuota = defaultQuota;
    }


    @Override
    public PeerResources getResourceLimits( final String peerId ) throws QuotaException
    {
        try
        {
            return localPeer.getResourceLimits( new PeerId( peerId ) );
        }
        catch ( PeerException e )
        {
            throw new QuotaException( e.getMessage() );
        }
    }


    @Override
    public ContainerQuota getQuota( final ContainerId containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );

        try
        {
            return localPeer.getQuota( containerId );
        }
        catch ( Exception e )
        {
            throw new QuotaException( e.getMessage() );
        }
    }


    @Override
    public void setQuota( final ContainerId containerId, final ContainerQuota containerQuota ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );
        Preconditions.checkNotNull( containerQuota, "Container quota cannot be null." );

        try
        {
            localPeer.setQuota( containerId, containerQuota );
        }
        catch ( PeerException e )
        {
            throw new QuotaException( e.getMessage() );
        }
    }


    @Override
    public void removeQuota( final ContainerId containerId )
    {
        localPeer.removeQuota( containerId );
    }


    @Override
    public ContainerQuota getDefaultContainerQuota( final ContainerSize containerSize )
    {
        return containerQuotas.get( containerSize );
    }


    @Override
    public Map<ContainerSize, ContainerQuota> getDefaultQuotas()
    {
        return Collections.unmodifiableMap( containerQuotas );
    }


    @Override
    public Set<Integer> getCpuSet( final ContainerId containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadCpuSetCommand( containerId.getContainerName() ) );

        Pattern p = Pattern.compile( "(\\s*\\d+\\s*-\\s*\\d+\\s*)" );
        StringTokenizer st = new StringTokenizer( result.getStdOut().trim(), "," );
        Set<Integer> cpuSet = Sets.newHashSet();

        while ( st.hasMoreTokens() )
        {
            String token = st.nextToken();
            Matcher m = p.matcher( token );
            if ( m.find() )
            {
                String[] range = m.group( 1 ).split( "-" );

                for ( int i = Integer.parseInt( range[0].trim() ); i <= Integer.parseInt( range[1].trim() ); i++ )
                {
                    cpuSet.add( i );
                }
            }
            else
            {
                cpuSet.add( Integer.valueOf( token.trim() ) );
            }
        }

        return cpuSet;
    }


    @Override
    public void setCpuSet( final ContainerId containerId, final Set<Integer> cpuSet ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( cpuSet ) );

        StringBuilder cpuSetString = new StringBuilder();
        for ( Integer cpuIdx : cpuSet )
        {
            cpuSetString.append( cpuIdx ).append( "," );
        }

        cpuSetString.replace( cpuSetString.length() - 1, cpuSetString.length(), "" );

        executeOnContainersResourceHost( containerId,
                commands.getWriteCpuSetCommand( containerId.getContainerName(), cpuSetString.toString() ) );
    }


    private CommandResult executeOnContainersResourceHost( ContainerId containerId, RequestBuilder command )
            throws QuotaException
    {
        try
        {
            ResourceHost resourceHost = localPeer.getResourceHostByContainerId( containerId.getId() );
            return commandUtil.execute( command, resourceHost );
        }
        catch ( HostNotFoundException | CommandException e )
        {
            throw new QuotaException( e );
        }
    }
}
