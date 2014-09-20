package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;

import com.google.common.base.Strings;


/**
 * Handles ZK config property removal
 */
public class RemovePropertyOperationHandler extends AbstractOperationHandler<ZookeeperImpl>
{
    private final String fileName;
    private final String propertyName;


    public RemovePropertyOperationHandler( ZookeeperImpl manager, String clusterName, String fileName,
                                           String propertyName )
    {
        super( manager, clusterName );
        this.fileName = fileName;
        this.propertyName = propertyName;
        productOperation = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Removing property %s from file %s", propertyName, fileName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }


    @Override
    public void run()
    {
        if ( Strings.isNullOrEmpty( clusterName ) || Strings.isNullOrEmpty( fileName ) || Strings
                .isNullOrEmpty( propertyName ) )
        {
            productOperation.addLogFailed( "Malformed arguments\nOperation aborted" );
            return;
        }
        final ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        productOperation.addLog( "Removing property..." );

        Command removePropertyCommand = Commands.getRemovePropertyCommand( fileName, propertyName, config.getNodes() );
        manager.getCommandRunner().runCommand( removePropertyCommand );

        if ( removePropertyCommand.hasSucceeded() )
        {
            productOperation.addLog( "Property removed successfully\nRestarting cluster..." );

            Command restartCommand = Commands.getRestartCommand( config.getNodes() );
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
                productOperation.addLogDone( "Cluster successfully restarted" );
            }
            else
            {
                productOperation.addLogFailed(
                        String.format( "Failed to restart cluster, %s", restartCommand.getAllErrors() ) );
            }
        }
        else
        {
            productOperation.addLogFailed(
                    String.format( "Removing property failed, %s", removePropertyCommand.getAllErrors() ) );
        }
    }
}
