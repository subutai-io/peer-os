package org.safehaus.subutai.core.lxc.quota.impl;


import org.json.JSONObject;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.quota.CpuQuotaInfo;
import org.safehaus.subutai.common.quota.HddQuotaInfo;
import org.safehaus.subutai.common.quota.Memory;
import org.safehaus.subutai.common.quota.MemoryQuotaInfo;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.core.lxc.quota.api.QuotaException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public class QuotaManagerImpl implements QuotaManager
{

    private PeerManager peerManager;
    private static Logger LOGGER = LoggerFactory.getLogger( QuotaManagerImpl.class );


    public QuotaManagerImpl( PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager );
        this.peerManager = peerManager;
    }


    @Override
    public void setQuota( String containerHostId, QuotaInfo quotaInfo ) throws QuotaException
    {
        Preconditions.checkNotNull( containerHostId, "ContainerName cannot be null" );
        Preconditions.checkNotNull( quotaInfo, "QuotaInfo cannot be null." );

        ContainerHost containerHost;
        try
        {
            containerHost = peerManager.getLocalPeer().getContainerHostByName( containerHostId );
        }
        catch ( HostNotFoundException e )
        {
            e.printStackTrace();
            throw new QuotaException( e );
        }

        String cmd = String.format( "subutai quota %s %s %s", containerHost.getHostname(), quotaInfo.getQuotaKey(),
                quotaInfo.getQuotaValue() );
        try
        {
            ResourceHost resourceHost =
                    peerManager.getLocalPeer().getResourceHostByContainerName( containerHost.getHostname() );
            RequestBuilder requestBuilder = new RequestBuilder( cmd );
            try
            {
                CommandResult commandResult = resourceHost.execute( requestBuilder );
                if ( commandResult == null || !commandResult.hasSucceeded() )
                {
                    throw new QuotaException( "Error setting quota value for command: " + cmd );
                }
            }
            catch ( CommandException ignored )
            {
                throw new QuotaException( "Error setting quota value for command: " + cmd, ignored );
            }
        }
        catch ( HostNotFoundException e )
        {
            throw new QuotaException( "Error setting quota value for command: " + cmd, e );
        }
    }


    @Override
    public PeerQuotaInfo getQuota( String containerHostId, QuotaType quotaType ) throws QuotaException
    {
        Preconditions.checkNotNull( quotaType, "QuotaType cannot be null." );
        Preconditions.checkNotNull( containerHostId, "ContainerName cannot be null." );

        ContainerHost containerHost;
        try
        {
            containerHost = peerManager.getLocalPeer().getContainerHostByName( containerHostId );
        }
        catch ( HostNotFoundException e )
        {
            throw new QuotaException( e );
        }

        String cmd = String.format( "subutai quota %s %s", containerHost.getHostname(), quotaType.getKey() );

        try
        {
            ResourceHost resourceHost =
                    peerManager.getLocalPeer().getResourceHostByContainerName( containerHost.getHostname() );
            CommandResult commandResult = resourceHost.execute( new RequestBuilder( cmd ) );

            if ( quotaType == QuotaType.QUOTA_ALL_JSON )
            {
                JSONObject jsonObject = new JSONObject( commandResult.getStdOut() );

                CpuQuotaInfo cpuQuota = new CpuQuotaInfo( jsonObject.getString( QuotaType.QUOTA_CPU_CPUS.getKey() ) );
                HddQuotaInfo hddHomeQuota = new HddQuotaInfo( "home",
                        new Memory( jsonObject.getString( QuotaType.QUOTA_HDD_HOME.getKey() ) ) );
                HddQuotaInfo hddVarQuota = new HddQuotaInfo( "var",
                        new Memory( jsonObject.getString( QuotaType.QUOTA_HDD_VAR.getKey() ) ) );
                HddQuotaInfo hddOptQuota = new HddQuotaInfo( "opt",
                        new Memory( jsonObject.getString( QuotaType.QUOTA_HDD_OPT.getKey() ) ) );
                HddQuotaInfo hddRootfsQuota = new HddQuotaInfo( "rootfs",
                        new Memory( jsonObject.getString( QuotaType.QUOTA_HDD_ROOTFS.getKey() ) ) );
                MemoryQuotaInfo memoryQuotaInfo = new MemoryQuotaInfo(
                        new Memory( jsonObject.getString( QuotaType.QUOTA_MEMORY_QUOTA.getKey() ) ) );

                return new PeerQuotaInfo( cpuQuota, hddHomeQuota, hddVarQuota, hddOptQuota, hddRootfsQuota,
                        memoryQuotaInfo );
            }
            else if ( quotaType == QuotaType.QUOTA_MEMORY_QUOTA )
            {
                Memory memory = new Memory( commandResult.getStdOut() );
                return new PeerQuotaInfo( null, null, null, null, null, new MemoryQuotaInfo( memory ) );
            }
            else if ( quotaType == QuotaType.QUOTA_CPU_CPUS )
            {
                CpuQuotaInfo cpuQuotaInfo = new CpuQuotaInfo( commandResult.getStdOut() );
                return new PeerQuotaInfo( cpuQuotaInfo, null, null, null, null, null );
            }
            return null;
        }
        catch ( CommandException | HostNotFoundException e )
        {
            LOGGER.error( "Error getting quota.", e );
            throw new QuotaException( e );
        }
        //        CommandRequest commandRequest = new CommandRequest( new RequestBuilder( cmd ), host.getId() );
    }
}
