package org.safehaus.subutai.core.lxc.quota.impl;


import org.json.JSONObject;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.quota.CpuQuotaInfo;
import org.safehaus.subutai.common.quota.HddQuotaInfo;
import org.safehaus.subutai.common.quota.MemoryQuotaInfo;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.core.lxc.quota.api.QuotaException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.peer.api.CommandUtil;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public class QuotaManagerImpl implements QuotaManager
{

    private static Logger LOGGER = LoggerFactory.getLogger( QuotaManagerImpl.class );
    private PeerManager peerManager;
    private CommandUtil commandUtil;


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

            if ( quotaType == QuotaType.QUOTA_ALL_JSON )
            {
                JSONObject jsonObject = new JSONObject( commandResult.getStdOut() );

                CpuQuotaInfo cpuQuota = new CpuQuotaInfo( jsonObject.getString( QuotaType.QUOTA_CPU_CPUS.getKey() ) );
                HddQuotaInfo hddHomeQuota =
                        new HddQuotaInfo( "home", jsonObject.getString( QuotaType.QUOTA_HDD_HOME.getKey() ) );
                HddQuotaInfo hddVarQuota =
                        new HddQuotaInfo( "var", jsonObject.getString( QuotaType.QUOTA_HDD_VAR.getKey() ) );
                HddQuotaInfo hddOptQuota =
                        new HddQuotaInfo( "opt", jsonObject.getString( QuotaType.QUOTA_HDD_OPT.getKey() ) );
                HddQuotaInfo hddRootfsQuota =
                        new HddQuotaInfo( "rootfs", jsonObject.getString( QuotaType.QUOTA_HDD_ROOTFS.getKey() ) );
                MemoryQuotaInfo memoryQuotaInfo =
                        new MemoryQuotaInfo( jsonObject.getString( QuotaType.QUOTA_MEMORY_QUOTA.getKey() ) );

                return new PeerQuotaInfo( cpuQuota, hddHomeQuota, hddVarQuota, hddOptQuota, hddRootfsQuota,
                        memoryQuotaInfo );
            }
            else if ( quotaType == QuotaType.QUOTA_MEMORY_QUOTA )
            {
                return new PeerQuotaInfo( new MemoryQuotaInfo( commandResult.getStdOut() ) );
            }
            else if ( quotaType == QuotaType.QUOTA_CPU_CPUS )
            {
                CpuQuotaInfo cpuQuotaInfo = new CpuQuotaInfo( commandResult.getStdOut() );
                return new PeerQuotaInfo( cpuQuotaInfo );
            }
            else
            {
                //TODO implement default case here
                throw new QuotaException( "Don't check one enum and let it be default" );
            }
        }
        catch ( CommandException | HostNotFoundException e )
        {
            LOGGER.error( "Error int getQuota.", e );
            throw new QuotaException( "Error getting quota value for command: " + cmd, e );
        }
    }
}
