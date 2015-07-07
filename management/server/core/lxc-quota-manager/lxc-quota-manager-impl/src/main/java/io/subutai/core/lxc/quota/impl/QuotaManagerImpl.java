package io.subutai.core.lxc.quota.impl;


import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.quota.CpuQuotaInfo;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.DiskQuotaUnit;
import org.safehaus.subutai.common.quota.QuotaException;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.quota.RamQuota;
import org.safehaus.subutai.common.quota.RamQuotaUnit;
import org.safehaus.subutai.common.util.CollectionUtil;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.peer.api.HostNotFoundException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.api.ResourceHost;
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
    public int getRamQuota( final UUID containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );

        ContainerHost containerHost = getContainerHostById( containerId );

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadRamQuotaCommand( containerHost.getHostname() ) );

        return Integer.parseInt( result.getStdOut().replace( "M", "" ).trim() );
    }


    @Override
    public RamQuota getRamQuotaInfo( final UUID containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );

        ContainerHost containerHost = getContainerHostById( containerId );

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadRamQuotaCommand( containerHost.getHostname() ) );

        return new RamQuota( RamQuotaUnit.MB, Integer.parseInt( result.getStdOut().replace( "M", "" ).trim() ) );
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

        return Integer.parseInt( result.getStdOut().trim() );
    }


    @Override
    public CpuQuotaInfo getCpuQuotaInfo( final UUID containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );

        ContainerHost containerHost = getContainerHostById( containerId );

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadCpuQuotaCommand( containerHost.getHostname() ) );

        return new CpuQuotaInfo( result.getStdOut().trim() );
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

        return DiskQuota.parse( diskPartition, result.getStdOut() );
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


    public void setRamQuota( final UUID containerId, final RamQuota ramQuota ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( ramQuota );

        ContainerHost containerHost = getContainerHostById( containerId );

        executeOnContainersResourceHost( containerId, commands.getWriteRamQuotaCommand2( containerHost.getHostname(),
                String.format( "%s%s", ramQuota.getRamQuotaValue(), ramQuota.getRamQuotaUnit().getAcronym() ) ) );
    }


    @Override
    public int getAvailableRamQuota( final UUID containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );

        ContainerHost containerHost = getContainerHostById( containerId );

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadAvailableRamQuotaCommand( containerHost.getHostname() ) );

        return Integer.parseInt( result.getStdOut().replace( "M", "" ).trim() );
    }


    @Override
    public int getAvailableCpuQuota( final UUID containerId ) throws QuotaException
    {
        Preconditions.checkNotNull( containerId );

        ContainerHost containerHost = getContainerHostById( containerId );

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadAvailableCpuQuotaCommand( containerHost.getHostname() ) );

        return Integer.parseInt( result.getStdOut().trim() );
    }


    @Override
    public DiskQuota getAvailableDiskQuota( final UUID containerId, final DiskPartition diskPartition )
            throws QuotaException
    {

        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( diskPartition );

        ContainerHost containerHost = getContainerHostById( containerId );

        CommandResult result = executeOnContainersResourceHost( containerId,
                commands.getReadAvailableDiskQuotaCommand( containerHost.getHostname(),
                        diskPartition.getPartitionName() ) );

        return DiskQuota.parse( diskPartition, result.getStdOut() );
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
            return peerManager.getLocalPeer().getContainerHostById( containerId );
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
            return peerManager.getLocalPeer().getResourceHostByContainerId( containerId );
        }
        catch ( HostNotFoundException e )
        {
            throw new QuotaException( String.format( "Resource host is not found by container id %s", containerId ) );
        }
    }


    @Override
    public QuotaInfo getQuotaInfo( UUID containerId, final QuotaType quotaType ) throws QuotaException
    {
        QuotaInfo quotaInfo = null;
        switch ( quotaType )
        {
            case QUOTA_TYPE_CPU:
                quotaInfo = getCpuQuotaInfo( containerId );
                break;
            case QUOTA_TYPE_DISK_ROOTFS:
                quotaInfo = getDiskQuota( containerId, DiskPartition.ROOT_FS );
                break;
            case QUOTA_TYPE_DISK_VAR:
                quotaInfo = getDiskQuota( containerId, DiskPartition.VAR );
                break;
            case QUOTA_TYPE_DISK_OPT:
                quotaInfo = getDiskQuota( containerId, DiskPartition.OPT );
                break;
            case QUOTA_TYPE_DISK_HOME:
                quotaInfo = getDiskQuota( containerId, DiskPartition.HOME );
                break;
            case QUOTA_TYPE_RAM:
                quotaInfo = getRamQuotaInfo( containerId );
                break;
        }
        return quotaInfo;
    }
}
