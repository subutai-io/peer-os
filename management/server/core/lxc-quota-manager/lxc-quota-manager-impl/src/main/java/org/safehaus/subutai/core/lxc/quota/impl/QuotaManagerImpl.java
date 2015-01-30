package org.safehaus.subutai.core.lxc.quota.impl;


import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.quota.CpuQuotaInfo;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.DiskQuotaUnit;
import org.safehaus.subutai.common.quota.HddQuotaInfo;
import org.safehaus.subutai.common.quota.MemoryQuotaInfo;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaException;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.peer.api.CommandUtil;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class QuotaManagerImpl implements QuotaManager
{

    private static Logger LOGGER = LoggerFactory.getLogger( QuotaManagerImpl.class );
    private PeerManager peerManager;
    private CommandUtil commandUtil;
    protected Commands commands = new Commands();


    public QuotaManagerImpl( PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager );
        this.peerManager = peerManager;
        this.commandUtil = new CommandUtil();
    }


    @Override
    public void setQuota( String containerName, QuotaInfo quotaInfo ) throws QuotaException
    {
        Preconditions.checkNotNull( containerName, "ContainerName cannot be null" );
        Preconditions.checkNotNull( quotaInfo, "QuotaInfo cannot be null." );


        String cmd = String.format( "subutai quota %s %s %s", containerName, quotaInfo.getQuotaKey(),
                quotaInfo.getQuotaValue() );
        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHostByContainerName( containerName );

            commandUtil.execute( new RequestBuilder( cmd ), resourceHost );
        }
        catch ( CommandException | HostNotFoundException e )
        {
            LOGGER.error( "Error in setQuota", e );
            throw new QuotaException( "Error setting quota value for command: " + cmd, e );
        }
    }


    @Override
    public PeerQuotaInfo getQuota( String containerName, QuotaType quotaType ) throws QuotaException
    {
        Preconditions.checkNotNull( quotaType, "QuotaType cannot be null." );
        Preconditions.checkNotNull( containerName, "ContainerName cannot be null." );

        String cmd = String.format( "subutai quota %s %s", containerName, quotaType.getKey() );

        try
        {
            ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHostByContainerName( containerName );
            CommandResult commandResult = commandUtil.execute( new RequestBuilder( cmd ), resourceHost );

            if ( quotaType == QuotaType.QUOTA_TYPE_ALL_JSON )
            {
                JSONObject jsonObject = new JSONObject( commandResult.getStdOut() );

                CpuQuotaInfo cpuQuota = new CpuQuotaInfo( jsonObject.getString( QuotaType.QUOTA_TYPE_CPU.getKey() ) );
                HddQuotaInfo hddHomeQuota = new HddQuotaInfo( DiskPartition.HOME,
                        jsonObject.getString( QuotaType.QUOTA_TYPE_DISK_HOME.getKey() ) );
                HddQuotaInfo hddVarQuota = new HddQuotaInfo( DiskPartition.VAR,
                        jsonObject.getString( QuotaType.QUOTA_TYPE_DISK_VAR.getKey() ) );
                HddQuotaInfo hddOptQuota = new HddQuotaInfo( DiskPartition.OPT,
                        jsonObject.getString( QuotaType.QUOTA_TYPE_DISK_OPT.getKey() ) );
                HddQuotaInfo hddRootfsQuota = new HddQuotaInfo( DiskPartition.ROOT_FS,
                        jsonObject.getString( QuotaType.QUOTA_TYPE_DISK_ROOTFS.getKey() ) );
                MemoryQuotaInfo memoryQuotaInfo =
                        new MemoryQuotaInfo( jsonObject.getString( QuotaType.QUOTA_TYPE_RAM.getKey() ) );

                return new PeerQuotaInfo( cpuQuota, hddHomeQuota, hddVarQuota, hddOptQuota, hddRootfsQuota,
                        memoryQuotaInfo );
            }
            else if ( quotaType == QuotaType.QUOTA_TYPE_RAM )
            {
                return new PeerQuotaInfo( new MemoryQuotaInfo( commandResult.getStdOut() ) );
            }
            else if ( quotaType == QuotaType.QUOTA_TYPE_CPU )
            {
                CpuQuotaInfo cpuQuotaInfo = new CpuQuotaInfo( commandResult.getStdOut() );
                return new PeerQuotaInfo( cpuQuotaInfo );
            }
            else if ( quotaType == QuotaType.QUOTA_TYPE_DISK_HOME )
            {
                return new PeerQuotaInfo( new HddQuotaInfo( DiskPartition.HOME, commandResult.getStdOut() ) );
            }
            else if ( quotaType == QuotaType.QUOTA_TYPE_DISK_OPT )
            {
                return new PeerQuotaInfo( new HddQuotaInfo( DiskPartition.OPT, commandResult.getStdOut() ) );
            }
            else if ( quotaType == QuotaType.QUOTA_TYPE_DISK_ROOTFS )
            {
                return new PeerQuotaInfo( new HddQuotaInfo( DiskPartition.ROOT_FS, commandResult.getStdOut() ) );
            }
            else if ( quotaType == QuotaType.QUOTA_TYPE_DISK_VAR )
            {
                //defaul QuotaType.QUOTA_TYPE_DISK_VAR
                return new PeerQuotaInfo( new HddQuotaInfo( DiskPartition.VAR, commandResult.getStdOut() ) );
            }
            else
            {
                LOGGER.error( "Error in getQuota. Unsupported quota type." );
                throw new QuotaException( "Specify correct quota type: " + cmd );
            }
        }
        catch ( CommandException | HostNotFoundException e )
        {
            LOGGER.error( "Error int getQuota.", e );
            throw new QuotaException( "Error getting quota value for command: " + cmd, e );
        }
    }


    @Override
    public int getRamQuota( final UUID containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );

        ContainerHost containerHost = getContainerHostById( containerId );

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadRamQuotaCommand( containerHost.getHostname() ) );

        return Integer.parseInt( result.getStdOut() );
    }


    @Override
    public void setRamQuota( final UUID containerId, final int ramInMb ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkArgument( ramInMb > 0 );

        ContainerHost containerHost = getContainerHostById( containerId );

        executeOnContainersResourceHost( containerId,
                commands.getWriteRamQuotaCommand( containerHost.getHostname(), ramInMb ) );
    }


    @Override
    public int getCpuQuota( final UUID containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );

        ContainerHost containerHost = getContainerHostById( containerId );

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadCpuQuotaCommand( containerHost.getHostname() ) );

        return Integer.parseInt( result.getStdOut() );
    }


    @Override
    public void setCpuQuota( final UUID containerId, final int cpuPercent ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkArgument( cpuPercent > 0 && cpuPercent <= 100 );

        ContainerHost containerHost = getContainerHostById( containerId );

        executeOnContainersResourceHost( containerId,
                commands.getWriteCpuQuotaCommand( containerHost.getHostname(), cpuPercent ) );
    }


    @Override
    public Set<Integer> getCpuSet( final UUID containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );

        ContainerHost containerHost = getContainerHostById( containerId );

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadCpuSetCommand( containerHost.getHostname() ) );

        Pattern p = Pattern.compile( "(\\s*\\d+\\s*-\\s*\\d+\\s*)" );
        StringTokenizer st = new StringTokenizer( result.getStdOut(), "," );
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
    public void setCpuSet( final UUID containerId, final Set<Integer> cpuSet ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( cpuSet ) );

        ContainerHost containerHost = getContainerHostById( containerId );

        StringBuilder cpuSetString = new StringBuilder();
        for ( Integer cpuIdx : cpuSet )
        {
            cpuSetString.append( cpuIdx ).append( "," );
        }

        cpuSetString.replace( cpuSetString.length() - 1, cpuSetString.length(), "" );

        executeOnContainersResourceHost( containerId,
                commands.getWriteCpuSetCommand( containerHost.getHostname(), cpuSetString.toString() ) );
    }


    @Override
    public DiskQuota getDiskQuota( final UUID containerId, DiskPartition diskPartition ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( diskPartition );

        ContainerHost containerHost = getContainerHostById( containerId );

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadDiskQuotaCommand( containerHost.getHostname(), diskPartition.getPartitionName() ) );

        if ( result.getStdOut().equalsIgnoreCase( DiskQuotaUnit.UNLIMITED.getAcronym() ) )
        {
            return new DiskQuota( diskPartition, DiskQuotaUnit.UNLIMITED, -1 );
        }
        else
        {
            String regex = "(\\d+)(K|M|G){0,1}";
            Pattern quotaPattern = Pattern.compile( regex );
            Matcher quotaMatcher = quotaPattern.matcher( result.getStdOut() );
            if ( quotaMatcher.matches() )
            {
                String quotaValue = quotaMatcher.group( 1 );
                long value = Long.parseLong( quotaValue );
                String acronym = quotaMatcher.group( 2 );
                DiskQuotaUnit diskQuotaUnit = DiskQuotaUnit.parseFromAcronym( acronym );
                return new DiskQuota( diskPartition, diskQuotaUnit == null ? DiskQuotaUnit.BYTE : diskQuotaUnit,
                        value );
            }
            else
            {
                throw new QuotaException( String.format( "Unparseable result: %s", result.getStdOut() ) );
            }
        }
    }


    @Override
    public void setDiskQuota( final UUID containerId, final DiskQuota diskQuota ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( diskQuota );

        ContainerHost containerHost = getContainerHostById( containerId );

        executeOnContainersResourceHost( containerId, commands.getWriteDiskQuotaCommand( containerHost.getHostname(),
                diskQuota.getDiskPartition().getPartitionName(), String.format( "%s%s",
                        diskQuota.getDiskQuotaUnit() == DiskQuotaUnit.UNLIMITED ? "" : diskQuota.getDiskQuotaValue(),
                        diskQuota.getDiskQuotaUnit().getAcronym() ) ) );
    }


    protected CommandResult executeOnContainersResourceHost( UUID containerId, RequestBuilder command )
            throws QuotaException
    {
        ResourceHost resourceHost = getResourceHostByContainerId( containerId );

        try
        {
            return commandUtil.execute( command, resourceHost );
        }
        catch ( CommandException e )
        {
            throw new QuotaException( e );
        }
    }


    protected ContainerHost getContainerHostById( UUID containerId ) throws QuotaException
    {
        try
        {
            return peerManager.getLocalPeer().getContainerHostById( containerId.toString() );
        }
        catch ( HostNotFoundException e )
        {
            throw new QuotaException( String.format( "Container host is not found by id %s", containerId ) );
        }
    }


    protected ResourceHost getResourceHostByContainerId( UUID containerId ) throws QuotaException
    {
        try
        {
            return peerManager.getLocalPeer().getResourceHostByContainerId( containerId.toString() );
        }
        catch ( HostNotFoundException e )
        {
            throw new QuotaException( String.format( "Resource host is not found by container id %s", containerId ) );
        }
    }
}
