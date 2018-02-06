package io.subutai.core.environment.impl.tasks;


import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.ContainerDto;
import io.subutai.common.environment.EnvironmentDto;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.settings.Common;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.hub.share.common.HubAdapter;


public class ContainerDiskUsageCheckTask implements Runnable
{
    private final static Logger LOG = LoggerFactory.getLogger( ContainerDiskUsageCheckTask.class );
    private final EnvironmentManagerImpl environmentManager;
    private final HubAdapter hubAdapter;
    private final LocalPeer localPeer;
    private final CommandUtil commandUtil = new CommandUtil();


    public ContainerDiskUsageCheckTask( final HubAdapter hubAdapter, final LocalPeer localPeer,
                                        final EnvironmentManagerImpl environmentManager )
    {
        this.environmentManager = environmentManager;
        this.hubAdapter = hubAdapter;
        this.localPeer = localPeer;
    }


    @Override
    public void run()
    {
        for ( EnvironmentDto environment : environmentManager.getTenantEnvironments() )
        {
            //iterate over hub containers
            if ( Common.HUB_ID.equals( environment.getDataSource() ) )
            {
                for ( ContainerDto container : environment.getContainers() )
                {
                    checkDiskUsage( container );

                    //pause not to stress RH with disk usage checks
                    TaskUtil.sleep( TimeUnit.SECONDS.toMillis( 5 ) );
                }
            }
        }
    }


    private void checkDiskUsage( ContainerDto containerDto )
    {
        // a. get its disk usage -> "subutai info du foo"
        // b. compare with container size disk quota limit
        //  b.a if du is >= 90 % of quota -> notify Hub
        //  b.b if du is >= 150 %  of quota -> stop container, notify Hub
        try
        {
            ResourceHost resourceHost = localPeer.getResourceHostById( containerDto.getRhId() );

            ContainerHost containerHost = localPeer.getContainerHostById( containerDto.getId() );

            CommandResult result = commandUtil
                    .execute( new RequestBuilder( "subutai info du " + containerDto.getContainerName() ),
                            resourceHost );

            long diskUsed = Long.parseLong( result.getStdOut().trim() );

            long diskLimit = containerHost.getContainerSize().getDiskQuota().longValue();

            if ( diskUsed >= diskLimit * 0.9 )
            {
                boolean stop = diskUsed >= diskLimit * 1.5;

                if ( stop )
                {
                    //stop container
                    containerHost.stop();
                }

                //notify Hub
                hubAdapter.notifyContainerDiskUsageExcess( containerDto.getPeerId(), containerDto.getEnvironmentId(),
                        containerDto.getId(), diskUsed, stop );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error checking disk usage of container {}: {}", containerDto.getContainerName(),
                    e.getMessage() );
        }
    }
}
