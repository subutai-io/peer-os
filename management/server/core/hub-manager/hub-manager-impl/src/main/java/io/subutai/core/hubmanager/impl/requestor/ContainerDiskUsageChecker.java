package io.subutai.core.hubmanager.impl.requestor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.ContainerDto;
import io.subutai.common.environment.EnvironmentDto;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.hubmanager.api.HubRequester;
import io.subutai.core.hubmanager.api.RestClient;


//https://github.com/subutai-io/agent/wiki/Switch-to-Soft-Quota
public class ContainerDiskUsageChecker extends HubRequester
{
    private final static Logger LOG = LoggerFactory.getLogger( ContainerDiskUsageChecker.class );

    private final EnvironmentManager environmentManager;
    private final LocalPeer localPeer;
    private final CommandUtil commandUtil = new CommandUtil();


    public ContainerDiskUsageChecker( final HubManager hubManager, final RestClient restClient,
                                      final EnvironmentManager environmentManager, final LocalPeer localPeer )
    {
        super( hubManager, restClient );
        this.environmentManager = environmentManager;
        this.localPeer = localPeer;
    }


    @Override
    public void request() throws Exception
    {
        for ( EnvironmentDto environment : environmentManager.getTenantEnvironments() )
        {
            //iterate over hub containers
            if ( Common.HUB_ID.equals( environment.getDataSource() ) )
            {
                for ( ContainerDto container : environment.getContainers() )
                {
                    checkDiskUsage( container );
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
            ContainerHost containerHost = localPeer.getContainerHostById( containerDto.getId() );

            CommandResult result = commandUtil
                    .execute( new RequestBuilder( "subutai info du " + containerDto.getContainerName() ),
                            containerHost );

            double diskUsed = Double.parseDouble( result.getStdOut() );

            Double diskLimit = containerHost.getContainerSize().getDiskQuota();

            if ( diskUsed >= diskLimit * 0.9 )
            {
                //notify Hub
                notifyHub( false );
            }
            else if ( diskUsed >= diskLimit * 1.5 )
            {
                //stop container
                containerHost.stop();

                //notify Hub
                notifyHub( true );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error checking disk usage of container " + containerDto.getContainerName(), e.getMessage() );
        }
    }


    private void notifyHub( final boolean containerWasStopped )
    {
        //TODO
    }
}
