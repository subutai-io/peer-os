package io.subutai.core.lxc.quota.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerType;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.Quota;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.quota.QuotaParser;
import io.subutai.common.quota.QuotaType;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.lxc.quota.impl.parser.CpuQuotaParser;
import io.subutai.core.lxc.quota.impl.parser.DiskQuotaParser;
import io.subutai.core.lxc.quota.impl.parser.RamQuotaParser;


public class QuotaManagerImpl implements QuotaManager
{

    private static Logger LOGGER = LoggerFactory.getLogger( QuotaManagerImpl.class );
    private LocalPeer localPeer;
    private CommandUtil commandUtil;
    protected Commands commands = new Commands();
    private Map<QuotaType, QuotaParser> registeredQuotaParsers = new HashMap<>();
    private HashMap<ContainerType, ContainerQuota> containerQuotas = new HashMap<>();
    private String defaultQuota;


    public QuotaManagerImpl( LocalPeer localPeer )
    {
        Preconditions.checkNotNull( localPeer );
        this.localPeer = localPeer;
        this.commandUtil = new CommandUtil();
    }


    public void init() throws QuotaException
    {
        registerQuotaParsers();
        initDefaultQuotas();
    }


    protected void registerQuotaParsers()
    {
        this.registeredQuotaParsers.put( QuotaType.QUOTA_TYPE_CPU, CpuQuotaParser.getInstance() );
        this.registeredQuotaParsers.put( QuotaType.QUOTA_TYPE_RAM, RamQuotaParser.getInstance() );
        this.registeredQuotaParsers
                .put( QuotaType.QUOTA_TYPE_DISK_OPT, DiskQuotaParser.getInstance( DiskPartition.OPT ) );
        this.registeredQuotaParsers
                .put( QuotaType.QUOTA_TYPE_DISK_HOME, DiskQuotaParser.getInstance( DiskPartition.HOME ) );
        this.registeredQuotaParsers
                .put( QuotaType.QUOTA_TYPE_DISK_VAR, DiskQuotaParser.getInstance( DiskPartition.VAR ) );
        this.registeredQuotaParsers
                .put( QuotaType.QUOTA_TYPE_DISK_ROOTFS, DiskQuotaParser.getInstance( DiskPartition.ROOT_FS ) );
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
        for ( ContainerType containerType : ContainerType.values() )
        {
            if ( i > ContainerType.values().length - 2 )
            {
                break;
            }
            LOGGER.debug( String.format( "Settings for %s: %s", containerType, settings[i] ) );
            String[] quotas = settings[i++].split( "\\|" );

            if ( quotas.length != 6 )
            {
                throw new QuotaException( String.format( "Invalid quota settings for %s.", containerType ) );
            }

            try
            {
                final ContainerQuota containerQuota = new ContainerQuota();

                QuotaParser quotaParser = getQuotaParser( QuotaType.QUOTA_TYPE_RAM );
                containerQuota.addQuota( quotaParser.parse( quotas[0] ) );

                quotaParser = getQuotaParser( QuotaType.QUOTA_TYPE_CPU );
                containerQuota.addQuota( quotaParser.parse( quotas[1] ) );

                quotaParser = getQuotaParser( QuotaType.QUOTA_TYPE_DISK_OPT );
                containerQuota.addQuota( quotaParser.parse( quotas[2] ) );

                quotaParser = getQuotaParser( QuotaType.QUOTA_TYPE_DISK_HOME );
                containerQuota.addQuota( quotaParser.parse( quotas[3] ) );

                quotaParser = getQuotaParser( QuotaType.QUOTA_TYPE_DISK_VAR );
                containerQuota.addQuota( quotaParser.parse( quotas[4] ) );

                quotaParser = getQuotaParser( QuotaType.QUOTA_TYPE_DISK_ROOTFS );
                containerQuota.addQuota( quotaParser.parse( quotas[5] ) );

                containerQuotas.put( containerType, containerQuota );

                LOGGER.debug( containerQuota.toString() );
            }
            catch ( Exception e )
            {
                throw new QuotaException( String.format( "Could not parse quota settings for %s.", containerType ) );
            }
        }
        LOGGER.info( "Quota settings parsed." );
    }


    public void setDefaultQuota( final String defaultQuota )
    {
        this.defaultQuota = defaultQuota;
    }


    @Override
    public void setQuota( ContainerId containerId, Quota quota ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );
        Preconditions.checkNotNull( quota, "Quota cannot be null." );

        CommandResult commandResult = executeOnContainersResourceHost( containerId,
                commands.getWriteQuotaCommand( containerId.getHostName(), quota ) );

    }


    @Override
    public void setQuota( final ContainerId containerId, final ContainerQuota quota ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );

        for ( Quota quotaInfo : quota.getAllQuotaInfo() )
        {
            setQuota( containerId, quotaInfo );
        }
    }


    @Override
    public ContainerQuota getQuota( final ContainerId containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );

        ContainerQuota containerQuota = new ContainerQuota();
        for ( QuotaType quotaType : registeredQuotaParsers.keySet() )
        {
            Quota quotaInfo = getQuota( containerId, quotaType );
            containerQuota.addQuota( quotaInfo );
        }
        return containerQuota;
    }


    @Override
    public Quota getQuota( final ContainerId containerId, QuotaType quotaType ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );
        Preconditions.checkNotNull( quotaType, "Quota type cannot be null." );

        QuotaParser quotaParser = registeredQuotaParsers.get( quotaType );
        if ( quotaParser == null )
        {
            throw new QuotaException( "Quota parser not registered for type: " + quotaType );
        }

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadQuotaCommand( containerId.getHostName(), quotaType ) );

        return quotaParser.parse( result.getStdOut() );
    }


    //    @Override
    //    public Quota getQuota( final ContainerId containerId, QuotaType quotaType ) throws QuotaException
    //    {
    //        Preconditions.checkNotNull( containerId, "ContainerName cannot be null" );
    //        Preconditions.checkNotNull( quotaType, "Quota type cannot be null." );
    //
    //        QuotaParser quotaParser = registeredQuotaParsers.get( quotaType );
    //        if ( quotaParser == null )
    //        {
    //            throw new QuotaException( "Quota parser not registered for type: " + quotaType );
    //        }
    //
    //        String cmd = String.format( "subutai quota %s %s", containerId, quotaType.getKey() );
    //        try
    //        {
    //            ResourceHost resourceHost = localPeer.getResourceHostByContainerId( containerId.getId() );
    //
    //            CommandResult result =
    //                    commandUtil.execute( commands.getReadQuotaCommand( containerId.getId(), quotaType ),
    // resourceHost );
    //
    //            return quotaParser.parse( result.getStdOut() );
    //        }
    //        catch ( CommandException | HostNotFoundException e )
    //        {
    //            LOGGER.error( "Error in setQuota", e );
    //            throw new QuotaException( "Error getting quota value for command: " + cmd, e );
    //        }
    //    }

    //
    //    @Override
    //    public int getRamQuota( final String containerId ) throws QuotaException
    //    {
    //        Preconditions.checkNotNull( containerId );
    //
    //        ContainerHost containerHost = getContainerHostById( containerId );
    //
    //        CommandResult result = executeOnContainersResourceHost( containerId,
    //                commands.getReadRamQuotaCommand( containerHost.getHostname() ) );
    //
    //        return Integer.parseInt( result.getStdOut().replace( "M", "" ).trim() );
    //    }
    //
    //
    //    @Override
    //    public RamQuota getRamQuota( final String containerId ) throws QuotaException
    //    {
    //        Preconditions.checkNotNull( containerId );
    //
    //        ContainerHost containerHost = getContainerHostById( containerId );
    //
    //        CommandResult result = executeOnContainersResourceHost( containerId,
    //                commands.getReadRamQuotaCommand( containerHost.getHostname() ) );
    //
    //        return new RamQuota( RamQuotaUnit.MB, Integer.parseInt( result.getStdOut().replace( "M", "" ).trim() ) );
    //    }


    //    @Override
    //    public void setRamQuota( final String containerId, final int ramInMb ) throws QuotaException
    //    {
    //        Preconditions.checkNotNull( containerId );
    //        Preconditions.checkArgument( ramInMb > 0 );
    //
    //        ContainerHost containerHost = getContainerHostById( containerId );
    //
    //        executeOnContainersResourceHost( containerId,
    //                commands.getWriteRamQuotaCommand( containerHost.getHostname(), ramInMb ) );
    //    }
    //
    //
    //    @Override
    //    public int getCpuQuota( final String containerId ) throws QuotaException
    //    {
    //        Preconditions.checkNotNull( containerId );
    //
    //        ContainerHost containerHost = getContainerHostById( containerId );
    //
    //        CommandResult result = executeOnContainersResourceHost( containerId,
    //                commands.getReadCpuQuotaCommand( containerHost.getHostname() ) );
    //
    //        return Integer.parseInt( result.getStdOut().trim() );
    //    }
    //
    //
    //    @Override
    //    public CpuQuota getCpuQuotaInfo( final String containerId ) throws QuotaException
    //    {
    //        Preconditions.checkNotNull( containerId );
    //
    //        ContainerHost containerHost = getContainerHostById( containerId );
    //
    //        CommandResult result = executeOnContainersResourceHost( containerId,
    //                commands.getReadCpuQuotaCommand( containerHost.getHostname() ) );
    //
    //        return CpuQuotaParser.getInstance().parse( result.getStdOut().trim() );
    //    }


    //    @Override
    //    public void setCpuQuota( final String containerId, final int cpuPercent ) throws QuotaException
    //    {
    //        Preconditions.checkNotNull( containerId );
    //        Preconditions.checkArgument( cpuPercent > 0 && cpuPercent <= 100 );
    //
    //        ContainerHost containerHost = getContainerHostById( containerId );
    //
    //        executeOnContainersResourceHost( containerId,
    //                commands.getWriteCpuQuotaCommand( containerHost.getHostname(), cpuPercent ) );
    //    }


    @Override
    public Set<Integer> getCpuSet( final ContainerId containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadCpuSetCommand( containerId.getHostName() ) );

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
                commands.getWriteCpuSetCommand( containerId.getHostName(), cpuSetString.toString() ) );
    }


    @Override
    public DiskQuota getDiskQuota( final ContainerId containerId, DiskPartition diskPartition ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );
        Preconditions.checkNotNull( diskPartition );

        QuotaType quotaType = findQuotaTypeByKey( diskPartition.getPartitionName() );

        QuotaParser parser = getQuotaParser( quotaType );
        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadQuotaCommand( containerId.getHostName(), quotaType ) );

        if ( result.hasSucceeded() )
        {
            return ( DiskQuota ) parser.parse( result.getStdOut() );
        }
        else
        {
            throw new QuotaException( "Error on getting disk quota." );
        }
    }


    @Override
    public DiskQuota getAvailableDiskQuota( final ContainerId containerId, final DiskPartition diskPartition )
            throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );
        Preconditions.checkNotNull( diskPartition );

        QuotaType quotaType = findQuotaTypeByKey( diskPartition.getPartitionName() );

        QuotaParser parser = getQuotaParser( quotaType );
        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadAvailableQuotaCommand( containerId.getHostName(), quotaType ) );

        if ( result.hasSucceeded() )
        {
            return ( DiskQuota ) parser.parse( result.getStdOut() );
        }
        else
        {
            throw new QuotaException( "Error on getting disk quota." );
        }
    }


    private QuotaType findQuotaTypeByKey( final String key )
    {
        QuotaType result = null;
        for ( QuotaType quotaType : DEFAULT_QUOTA_TYPES )
        {
            if ( quotaType.getKey().equalsIgnoreCase( key ) )
            {
                result = quotaType;
                break;
            }
        }

        return result;
    }


    //    @Override
    //    public void setDiskQuota( final String containerId, final DiskQuota diskQuota ) throws QuotaException
    //    {
    //        Preconditions.checkNotNull( containerId );
    //        Preconditions.checkNotNull( diskQuota );
    //
    //        ContainerHost containerHost = getContainerHostById( containerId );
    //
    //        executeOnContainersResourceHost( containerId, commands.getWriteDiskQuotaCommand( containerHost
    // .getHostname(),
    //                diskQuota.getDiskPartition().getPartitionName(), String.format( "%s%s",
    //                        diskQuota.getDiskQuotaUnit() == DiskQuotaUnit.UNLIMITED ? "" : diskQuota
    // .getDiskQuotaValue(),
    //                        diskQuota.getDiskQuotaUnit().getAcronym() ) ) );
    //    }
    //
    //
    //    public void setRamQuota( final String containerId, final RamQuota ramQuotaInfo ) throws QuotaException
    //    {
    //        Preconditions.checkNotNull( containerId );
    //        Preconditions.checkNotNull( ramQuotaInfo );
    //
    //        ContainerHost containerHost = getContainerHostById( containerId );
    //
    //        executeOnContainersResourceHost( containerId, commands.getWriteRamQuotaCommand2( containerHost
    // .getHostname(),
    //                String.format( "%s%s", ramQuotaInfo.getRamQuotaValue(),
    //                        ramQuotaInfo.getRamQuotaUnit().getAcronym() ) ) );
    //    }
    //


    @Override
    public Quota getAvailableQuota( final ContainerId containerId, final QuotaType quotaType ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );

        QuotaParser parser = getQuotaParser( quotaType );

        Quota result = null;
        CommandResult commandResult = executeOnContainersResourceHost( containerId,
                commands.getReadAvailableQuotaCommand( containerId.getHostName(), quotaType ) );
        if ( commandResult.hasSucceeded() )
        {
            result = parser.parse( commandResult.getStdOut() );
        }
        else
        {
            throw new QuotaException( "Error on getting available quota value." );
        }


        return result;
    }


    //    @Override
    //    public int getAvailableRamQuota( final String containerId ) throws QuotaException
    //    {
    //        Preconditions.checkNotNull( containerId );
    //
    //        ContainerHost containerHost = getContainerHostById( containerId );
    //
    //        CommandResult result = executeOnContainersResourceHost( containerId,
    //                commands.getReadAvailableRamQuotaCommand( containerHost.getHostname() ) );
    //
    //        return Integer.parseInt( result.getStdOut().replace( "M", "" ).trim() );
    //    }
    //
    //
    //    @Override
    //    public int getAvailableCpuQuota( final String containerId ) throws QuotaException
    //    {
    //        Preconditions.checkNotNull( containerId );
    //
    //        ContainerHost containerHost = getContainerHostById( containerId );
    //
    //        CommandResult result = executeOnContainersResourceHost( containerId,
    //                commands.getReadAvailableCpuQuotaCommand( containerHost.getHostname() ) );
    //
    //        return Integer.parseInt( result.getStdOut().trim() );
    //    }
    //
    //
    //    @Override
    //    public DiskQuota getAvailableDiskQuota( final String containerId, final DiskPartition diskPartition )
    //            throws QuotaException
    //    {
    //
    //        Preconditions.checkNotNull( containerId );
    //        Preconditions.checkNotNull( diskPartition );
    //
    //        ContainerHost containerHost = getContainerHostById( containerId );
    //
    //        CommandResult result = executeOnContainersResourceHost( containerId,
    //                commands.getReadAvailableDiskQuotaCommand( containerHost.getHostname(),
    //                        diskPartition.getPartitionName() ) );
    //
    //        return DiskQuotaParser.getInstance( diskPartition ).parse( result.getStdOut() );
    //    }
    //


    protected CommandResult executeOnContainersResourceHost( ContainerId containerId, RequestBuilder command )
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
    //
    //
    //    protected ContainerHost getContainerHostById( String containerId ) throws QuotaException
    //    {
    //        try
    //        {
    //            return localPeer.getContainerHostById( containerId );
    //        }
    //        catch ( HostNotFoundException e )
    //        {
    //            throw new QuotaException( String.format( "Container host is not found by id %s", containerId ) );
    //        }
    //    }
    //
    //
    //    protected ResourceHost getResourceHostByContainerId( String containerId ) throws QuotaException
    //    {
    //        try
    //        {
    //            return localPeer.getResourceHostByContainerId( containerId );
    //        }
    //        catch ( HostNotFoundException e )
    //        {
    //            throw new QuotaException( String.format( "Resource host is not found by container id %s",
    // containerId ) );
    //        }
    //    }

    //
    //    @Override
    //    public Quota getQuotaInfo( String containerId, final QuotaType quotaType ) throws QuotaException
    //    {
    //        Quota quotaInfo = null;
    //        switch ( quotaType )
    //        {
    //            case QUOTA_TYPE_CPU:
    //                quotaInfo = getCpuQuotaInfo( containerId );
    //                break;
    //            case QUOTA_TYPE_DISK_ROOTFS:
    //                quotaInfo = getDiskQuota( containerId, DiskPartition.ROOT_FS );
    //                break;
    //            case QUOTA_TYPE_DISK_VAR:
    //                quotaInfo = getDiskQuota( containerId, DiskPartition.VAR );
    //                break;
    //            case QUOTA_TYPE_DISK_OPT:
    //                quotaInfo = getDiskQuota( containerId, DiskPartition.OPT );
    //                break;
    //            case QUOTA_TYPE_DISK_HOME:
    //                quotaInfo = getDiskQuota( containerId, DiskPartition.HOME );
    //                break;
    //            case QUOTA_TYPE_RAM:
    //                quotaInfo = getRamQuota( containerId );
    //                break;
    //        }
    //        return quotaInfo;
    //    }


    @Override
    public QuotaParser getQuotaParser( final QuotaType quotaType ) throws QuotaException
    {
        final QuotaParser parser = registeredQuotaParsers.get( quotaType );
        if ( parser == null )
        {
            throw new QuotaException( "Quota parser not registered for type: " + quotaType );
        }

        return parser;
    }


    @Override
    public ContainerQuota getDefaultContainerQuota( final ContainerType containerType )
    {
        return containerQuotas.get( containerType );
    }


    @Override
    public void setQuota( final ContainerId containerId, final QuotaType quotaType, final String quotaValue )
            throws QuotaException
    {
        Preconditions.checkNotNull( containerId, "Container ID cannot be null" );

        final QuotaParser parser = getQuotaParser( quotaType );

        setQuota( containerId, parser.parse( quotaValue ) );
    }
}
