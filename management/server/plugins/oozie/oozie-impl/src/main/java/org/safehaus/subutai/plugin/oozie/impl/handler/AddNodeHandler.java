package org.safehaus.subutai.plugin.oozie.impl.handler;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.SetupType;
import org.safehaus.subutai.plugin.oozie.impl.Commands;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;

import com.google.common.collect.Sets;


public class AddNodeHandler extends AbstractOperationHandler<OozieImpl, OozieClusterConfig>
{

    private final String hostname;


    public AddNodeHandler( OozieImpl manager, String clusterName, String hostname )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        trackerOperation = manager.getTracker().createTrackerOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Adding node %s to %s", ( hostname != null ? hostname : "" ), clusterName ) );
    }


    @Override
    public void run()
    {
        /*TrackerOperation po = trackerOperation;
        OozieClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        try
        {
            Agent agent;
            if ( config.getSetupType() == SetupType.OVER_HADOOP )
            {
                agent = setupHost( config );
            }
            else if ( config.getSetupType() == SetupType.WITH_HADOOP )
            {
                agent = addHost( config );
            }
            else
            {
                throw new ClusterSetupException( "No setup type" );
            }

            config.getClients().add( agent );

            po.addLog( "Saving cluster info..." );
            manager.getPluginDAO().saveInfo( OozieClusterConfig.PRODUCT_KEY, clusterName, config );
            po.addLog( "Saved cluster info" );

            po.addLogDone( null );
        }
        catch ( ClusterSetupException ex )
        {
            po.addLog( ex.getMessage() );
            po.addLogFailed( "Add client node failed" );
        }*/
    }


   /* public Agent setupHost( OozieClusterConfig config ) throws ClusterSetupException
    {
        TrackerOperation po = trackerOperation;

        Agent agent = manager.getAgentManager().getAgentByHostname( hostname );
        if ( agent == null )
        {
            throw new ClusterSetupException( "New node is not connected" );
        }

        //check if node is in the cluster
        if ( config.getClients().contains( agent ) )
        {
            throw new ClusterSetupException( "Node already belongs to cluster" + clusterName );
        }

        po.addLog( "Checking prerequisites..." );

        Command checkInstalledCommand = manager.getCommands().getCheckInstalledCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            throw new ClusterSetupException( "Failed to check installed packages" );
        }

        AgentResult result = checkInstalledCommand.getResults().get( agent.getUuid() );
        boolean skipInstall = false;
        String hadoopPack = Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME;
        if ( result.getStdOut().contains( Commands.CLIENT_PACKAGE_NAME ) )
        {
            skipInstall = true;
            po.addLog( "Node already has Oozie client installed" );
        }
        else if ( !result.getStdOut().contains( hadoopPack ) )
        {
            throw new ClusterSetupException( "Node has no Hadoop installation" );
        }

        //install mahout
        if ( !skipInstall )
        {
            po.addLog( "Installing Oozie Client..." );
            Command installCommand = manager.getCommands().getInstallClientCommand( Sets.newHashSet( agent ) );
            manager.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() )
            {
                po.addLog( "Installation succeeded" );
            }
            else
            {
                throw new ClusterSetupException( "Installation failed: " + installCommand.getAllErrors() );
            }
        }
        return agent;
    }


    public Agent addHost( OozieClusterConfig config )
    {

        return null;
    }*/
}
