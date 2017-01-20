package io.subutai.core.lxc.quota.impl;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerPolicy;
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
import io.subutai.hub.share.resource.CpuResource;
import io.subutai.hub.share.resource.DiskResource;
import io.subutai.hub.share.resource.HostResources;
import io.subutai.hub.share.resource.PeerResources;
import io.subutai.hub.share.resource.RamResource;
import io.subutai.hub.share.resource.ResourceValueParser;


public class QuotaManagerImpl implements QuotaManager
{
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf( 100 );
    private static final long QUOTA_TTL_MIN = 5;

    private static Logger LOGGER = LoggerFactory.getLogger( QuotaManagerImpl.class );
    private LocalPeer localPeer;
    private PeerManager peerManager;
    private CommandUtil commandUtil;
    private Commands commands = new Commands();
    private EnumMap<ContainerSize, ContainerQuota> containerQuotas = new EnumMap<>( ContainerSize.class );
    private String defaultQuota;
    private ScheduledExecutorService quotaCachePopulator = Executors.newSingleThreadScheduledExecutor();


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
        startCachePopulator();
        initDefaultQuotas();
    }


    private void startCachePopulator()
    {
        quotaCachePopulator.scheduleWithFixedDelay( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    for ( ContainerHost containerHost : localPeer.getRegisteredContainers() )
                    {
                        containerQuotasCache.refresh( containerHost.getContainerId() );
                    }
                }
                catch ( Exception e )
                {
                    LOGGER.warn( "Error updating cache: {}", e.getMessage() );
                }
            }
        }, 1, QUOTA_TTL_MIN, TimeUnit.MINUTES );
    }


    private void initDefaultQuotas() throws QuotaException
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


    /**
     * Returns limits for requested peer
     *
     * @param peerId peer id
     */
    @Override
    public PeerResources getResourceLimits( String peerId )
    {
        PeerPolicy policy = peerManager.getPolicy( peerId );

        int environmentLimit = policy.getEnvironmentLimit();
        int containerLimit = policy.getContainerLimit();
        int networkLimit = policy.getNetworkUsageLimit();

        Set<String> environments = new HashSet<>();
        final List<ContainerHost> peerContainers = localPeer.getPeerContainers( peerId );
        for ( ContainerHost containerHost : peerContainers )
        {
            environments.add( containerHost.getEnvironmentId().getId() );
        }

        environmentLimit -= environments.size();
        containerLimit -= peerContainers.size();

        ResourceHostMetrics metrics = null;

        try
        {
            metrics = localPeer.getResourceHostMetrics();
        }
        catch ( PeerException e )
        {
            LOGGER.error( e.getMessage() );
        }

        List<HostResources> resources = new ArrayList<>();

        if ( metrics != null )
        {
            for ( ResourceHostMetric resourceHostMetric : metrics.getResources() )
            {
                try
                {
                    ResourceHost resourceHost = localPeer.getResourceHostByHostName( resourceHostMetric.getHostName() );
                    BigDecimal[] usedResources = getUsedResources();

                    BigDecimal cpuLimit = getCpuLimit( policy );

                    BigDecimal ramLimit = getRamLimit( new BigDecimal( resourceHostMetric.getTotalRam() ), policy );

                    BigDecimal diskLimit = getDiskLimit( new BigDecimal( resourceHostMetric.getTotalSpace() ), policy );

                    CpuResource cpuResource = new CpuResource( cpuLimit.subtract( usedResources[0] ), 0.0, "UNKNOWN",
                            resourceHostMetric.getCpuCore(), 0, 0, 0, resourceHostMetric.getCpuFrequency(), 0 );

                    RamResource ramResource = new RamResource( ramLimit.subtract( usedResources[1] ), 0.0 );

                    DiskResource diskResource =
                            new DiskResource( diskLimit.subtract( usedResources[2] ), 0.0, "UNKNOWN", 0.0, 0.0, false );


                    HostResources hostResources =
                            new HostResources( resourceHost.getId(), cpuResource, ramResource, diskResource );
                    resources.add( hostResources );
                }
                catch ( Exception e )
                {
                    // ignore
                    LOGGER.warn( e.getMessage() );
                }
            }
        }

        return new PeerResources( localPeer.getId(), environmentLimit, containerLimit, networkLimit, resources );
    }


    private BigDecimal getRamLimit( final BigDecimal total, final PeerPolicy peerPolicy )
    {
        return percentage( total, new BigDecimal( peerPolicy.getMemoryUsageLimit() ) );
    }


    private BigDecimal getDiskLimit( final BigDecimal total, final PeerPolicy peerPolicy )
    {
        return percentage( total, new BigDecimal( peerPolicy.getDiskUsageLimit() ) );
    }


    private BigDecimal getCpuLimit( final PeerPolicy peerPolicy )
    {
        return percentage( ONE_HUNDRED, new BigDecimal( peerPolicy.getCpuUsageLimit() ) );
    }


    private BigDecimal[] getUsedResources() throws QuotaException
    {
        BigDecimal cpuAccumulo = BigDecimal.ZERO;
        BigDecimal ramAccumulo = BigDecimal.ZERO;
        BigDecimal diskAccumulo = BigDecimal.ZERO;
        // todo: extract from DB

        return new BigDecimal[] { cpuAccumulo, ramAccumulo, diskAccumulo };
    }


    private static BigDecimal percentage( BigDecimal base, BigDecimal pct )
    {
        return base.multiply( pct ).divide( ONE_HUNDRED, BigDecimal.ROUND_UP );
    }


    @Override
    public ContainerQuota getQuota( final ContainerId containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );

        try
        {
            return containerQuotasCache.get( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting quota for container {}: {}", containerId.getContainerName(), e.getMessage() );

            throw new QuotaException( e.getMessage() );
        }
    }


    private LoadingCache<ContainerId, ContainerQuota> containerQuotasCache =
            CacheBuilder.newBuilder().maximumSize( 1000 ).expireAfterWrite( QUOTA_TTL_MIN, TimeUnit.MINUTES ).build(

                    new CacheLoader<ContainerId, ContainerQuota>()
                    {
                        @Override
                        public ContainerQuota load( final ContainerId containerId ) throws Exception
                        {
                            return localPeer.getQuota( containerId );
                        }
                    } );


    @Override
    public void setQuota( final ContainerId containerId, final ContainerQuota containerQuota ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );
        Preconditions.checkNotNull( containerQuota, "Container quota cannot be null." );

        executeOnContainersResourceHost( containerId,
                commands.getSetQuotaCommand( containerId.getContainerName(), containerQuota ) );
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
