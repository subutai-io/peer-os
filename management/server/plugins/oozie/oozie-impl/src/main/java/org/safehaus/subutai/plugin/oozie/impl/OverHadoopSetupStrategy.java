package org.safehaus.subutai.plugin.oozie.impl;


import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;

import com.google.common.collect.Sets;


public class OverHadoopSetupStrategy extends OozieSetupStrategy
{


    public OverHadoopSetupStrategy( OozieImpl manager, TrackerOperation po, OozieClusterConfig config )
    {
        super( manager, po, config );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        //check if node agents are connected
        /*for ( Agent agent : config.getClients() )
        {
            String hostname = agent.getHostname();
            if ( oozieManager.getAgentManager().getAgentByHostname( hostname ) == null )
            {
                throw new ClusterSetupException( String.format( "Node %s is not connected", hostname ) );
            }
        }
        String serverHostName = config.getServer().getHostname();
        if ( oozieManager.getAgentManager().getAgentByHostname( serverHostName ) == null )
        {
            throw new ClusterSetupException( String.format( "Node %s is not connected", serverHostName ) );
        }

        HadoopClusterConfig hc = oozieManager.getHadoopManager().getCluster( config.getHadoopClusterName() );
        if ( hc == null )
        {
            throw new ClusterSetupException( "Could not find Hadoop cluster " + config.getHadoopClusterName() );
        }


        Set<Agent> allOozieAgents = config.getAllOozieAgents();

        if ( !hc.getAllNodes().containsAll( allOozieAgents ) )
        {
            throw new ClusterSetupException(
                    "Not all nodes belong to Hadoop cluster " + config.getHadoopClusterName() );
        }

        Command cmd = oozieManager.getCommandRunner().createCommand(
                new RequestBuilder( oozieManager.getCommands().make( CommandType.STATUS ) ), allOozieAgents );
        oozieManager.getCommandRunner().runCommand( cmd );
        if ( !cmd.hasSucceeded() )
        {
            throw new ClusterSetupException( "Failed to check installed packages" );
        }

        po.addLog( String.format( "Installing Oozie server on %s...", config.getServer().getHostname() ) );
        String sserver = Commands.make( CommandType.INSTALL_SERVER );
        Agent serverAgent = config.getServer();
        cmd = oozieManager.getCommandRunner().createCommand( new RequestBuilder( sserver ).withTimeout( 1800 ),
                Sets.newHashSet( serverAgent ) );
        oozieManager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            po.addLog( "Installation of server succeeded" );
        }
        else
        {
            throw new ClusterSetupException( "Installation failed: " + cmd.getAllErrors() );
        }


        if ( !config.getClients().isEmpty() )
        {
            po.addLog( "Installing Oozie client ..." );
            String sclient = Commands.make( CommandType.INSTALL_CLIENT );

            Set<Agent> clients = config.getClients();
            cmd = oozieManager.getCommandRunner()
                              .createCommand( new RequestBuilder( sclient ).withTimeout( 1800 ), clients );
            oozieManager.getCommandRunner().runCommand( cmd );

            if ( cmd.hasSucceeded() )
            {
                po.addLog( "Installation of clients succeeded" );
            }
            else
            {
                throw new ClusterSetupException( "Installation of clients failed: " + cmd.getAllErrors() );
            }
        }
        else
        {
            po.addLog( "No client is selected, continuing" );
        }

        po.addLog( "Saving to db..." );
        oozieManager.getPluginDAO().saveInfo( OozieClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLog( "Cluster info successfully saved" );
*/
        return config;
    }
}
