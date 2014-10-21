package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;

import com.google.common.base.Strings;


/**
 * Handles ZK config property addition
 */
public class AddPropertyOperationHandler extends AbstractOperationHandler<ZookeeperImpl>
{
    private final String fileName;
    private final String propertyName;
    private final String propertyValue;


    public AddPropertyOperationHandler( ZookeeperImpl manager, String clusterName, String fileName, String propertyName,
                                        String propertyValue )
    {
        super( manager, clusterName );
        this.fileName = fileName;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        trackerOperation = manager.getTracker().createTrackerOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Adding property %s=%s to file %s", propertyName, propertyValue, fileName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {
        if ( Strings.isNullOrEmpty( clusterName ) || Strings.isNullOrEmpty( fileName ) || Strings
                .isNullOrEmpty( propertyName ) )
        {
            trackerOperation.addLogFailed( "Malformed arguments\nOperation aborted" );
            return;
        }
        final ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        trackerOperation.addLog( "Adding property..." );

        Command addPropertyCommand =
                manager.getCommands().getAddPropertyCommand( fileName, propertyName, propertyValue, config.getNodes() );
        manager.getCommandRunner().runCommand( addPropertyCommand );

        if ( addPropertyCommand.hasSucceeded() )
        {
            trackerOperation.addLog( "Property added successfully\nRestarting cluster..." );

            Command restartCommand = manager.getCommands().getRestartCommand( config.getNodes() );
            final AtomicInteger count = new AtomicInteger();
            manager.getCommandRunner().runCommand( restartCommand, new CommandCallback()
            {
                @Override
                public void onResponse( Response response, AgentResult agentResult, Command command )
                {
                    if ( agentResult.getStdOut().contains( "STARTED" ) )
                    {
                        if ( count.incrementAndGet() == config.getNodes().size() )
                        {
                            stop();
                        }
                    }
                }
            } );

            if ( count.get() == config.getNodes().size() )
            {
                trackerOperation.addLogDone( "Cluster successfully restarted" );
            }
            else
            {
                trackerOperation.addLogFailed(
                        String.format( "Failed to restart cluster, %s", restartCommand.getAllErrors() ) );
            }
        }
        else
        {
            trackerOperation
                    .addLogFailed( String.format( "Adding property failed, %s", addPropertyCommand.getAllErrors() ) );
        }
    }
}
