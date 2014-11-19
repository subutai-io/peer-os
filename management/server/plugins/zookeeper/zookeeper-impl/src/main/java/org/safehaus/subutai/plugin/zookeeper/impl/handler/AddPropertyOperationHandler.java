package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;

import com.google.common.base.Strings;


/**
 * Handles ZK config property addition
 */
public class AddPropertyOperationHandler extends AbstractOperationHandler<ZookeeperImpl, ZookeeperClusterConfig>
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


        Environment zookeeperEnvironment = manager.getEnvironmentManager().
                getEnvironmentByUUID( config.getEnvironmentId() );
        Set<ContainerHost> zookeeperNodes = zookeeperEnvironment.getHostsByIds( config.getNodes() );


        String addPropertyCommand =
                manager.getCommands().getAddPropertyCommand( fileName, propertyName, propertyValue );


        List<CommandResult> commandResultList = runCommandOnContainers( addPropertyCommand, zookeeperNodes );

        if ( getFailedCommandResults( commandResultList ).size() == 0 )
        {
            trackerOperation.addLog( "Property added successfully\nRestarting cluster..." );

            String restartCommand = manager.getCommands().getRestartCommand();

            commandResultList = runCommandOnContainers( restartCommand, zookeeperNodes );
            trackerOperation.addLogDone( "Restarting cluster finished..." );
        }
        else
        {
            StringBuilder stringBuilder = new StringBuilder();
            for ( CommandResult commandResult : getFailedCommandResults( commandResultList ) )
            {
                stringBuilder.append( commandResult.getStdErr() );
            }
            trackerOperation.addLogFailed(
                    String.format( "Removing property failed: %s", stringBuilder.toString() ) );
        }
    }


    private List<CommandResult> runCommandOnContainers( String command, final Set<ContainerHost> zookeeperNodes )
    {
        List<CommandResult> commandResults = new ArrayList<>();
        for ( ContainerHost containerHost : zookeeperNodes ) {
            try
            {
                commandResults.add( containerHost.execute( new RequestBuilder( command ) ) );
            }
            catch ( CommandException e )
            {
                e.printStackTrace();
            }
        }
        return commandResults;
    }



    public List<CommandResult> getFailedCommandResults( final List<CommandResult> commandResultList )
    {
        List<CommandResult> failedCommands = new ArrayList<>();
        for ( CommandResult commandResult : commandResultList ) {
            if ( ! commandResult.hasSucceeded() )
                failedCommands.add( commandResult );
        }
        return failedCommands;
    }

}
