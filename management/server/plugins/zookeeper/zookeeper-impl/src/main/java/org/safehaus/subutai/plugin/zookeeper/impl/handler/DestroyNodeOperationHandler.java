package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;

import com.google.common.collect.Sets;


/**
 * Handles destroy node operation
 */
public class DestroyNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl>
{
    private final String lxcHostname;


    public DestroyNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        trackerOperation = manager.getTracker().createTrackerOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {
        final ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            trackerOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !config.getNodes().contains( agent ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        if ( config.getNodes().size() == 1 )
        {
            trackerOperation.addLogFailed( "This is the last node in the cluster. Please, destroy cluster instead" );
            return;
        }

        config.getNodes().remove( agent );
        config.setNumberOfNodes( config.getNumberOfNodes() - 1 );

        try
        {
            new ClusterConfiguration( manager, trackerOperation ).configureCluster( config );
        }
        catch ( ClusterConfigurationException e )
        {
            trackerOperation.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
            return;
        }

        if ( config.getSetupType() == SetupType.STANDALONE )
        {
            //destroy lxc
            trackerOperation.addLog( "Destroying lxc container..." );
            Agent physicalAgent = manager.getAgentManager().getAgentByHostname( agent.getParentHostName() );
            if ( physicalAgent == null )
            {
                trackerOperation.addLog( String.format(
                        "Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
                        agent.getHostname() ) );
            }
            else
            {

                try
                {
                    manager.getContainerManager().cloneDestroy( physicalAgent.getHostname(), agent.getHostname() );
                    trackerOperation.addLog( "Lxc container destroyed successfully" );
                }
                catch ( LxcDestroyException e )
                {
                    trackerOperation.addLog(
                            String.format( "Could not destroy lxc container %s. Use LXC module to cleanup, skipping...",
                                    e.getMessage() ) );
                }
            }
        }
        else
        {
            //just uninstall Zookeeper
            trackerOperation.addLog( String.format( "Uninstalling %s", ZookeeperClusterConfig.PRODUCT_NAME ) );

            Command uninstallCommand = manager.getCommands().getUninstallCommand( Sets.newHashSet( agent ) );
            manager.getCommandRunner().runCommand( uninstallCommand );

            if ( uninstallCommand.hasCompleted() )
            {
                if ( uninstallCommand.hasSucceeded() )
                {
                    trackerOperation.addLog( "Cluster successfully uninstalled" );
                }
                else
                {
                    trackerOperation.addLog( String.format( "Uninstallation failed, %s, skipping...",
                            uninstallCommand.getAllErrors() ) );
                }
            }
            else
            {
                trackerOperation.addLog( "Uninstallation failed, command timed out, skipping..." );
            }
        }


        //update db
        trackerOperation.addLog( "Updating cluster information in database..." );

        manager.getPluginDAO().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        trackerOperation.addLogDone( "Cluster information updated in database" );
    }
}
