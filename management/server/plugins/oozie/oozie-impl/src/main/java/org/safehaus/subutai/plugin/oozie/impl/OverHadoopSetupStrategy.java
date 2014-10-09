package org.safehaus.subutai.plugin.oozie.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;

import com.google.common.collect.Sets;


public class OverHadoopSetupStrategy extends OozieSetupStrategy
{


    public OverHadoopSetupStrategy( OozieImpl manager, ProductOperation po, OozieClusterConfig config )
    {
        super( manager, po, config );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        //check if node agents are connected
        for ( String hostname : config.getClients() )
        {
            if ( oozieManager.getAgentManager().getAgentByHostname( hostname ) == null )
            {
                throw new ClusterSetupException( String.format( "Node %s is not connected", hostname ) );
            }
        }

        if ( oozieManager.getAgentManager().getAgentByHostname( config.getServer() ) == null )
        {
            throw new ClusterSetupException( String.format( "Node %s is not connected", config.getServer() ) );
        }

        HadoopClusterConfig hc = oozieManager.getHadoopManager().getCluster( config.getHadoopClusterName() );
        if ( hc == null )
        {
            throw new ClusterSetupException( "Could not find Hadoop cluster " + config.getHadoopClusterName() );
        }

        Set<String> allOozieHostnames = config.getAllOozieAgents();

        Set<Agent> allOozieAgents = new HashSet<>();

        for ( String agentHostname : allOozieHostnames )
        {
            Agent agent = oozieManager.getAgentManager().getAgentByHostname( agentHostname );
            allOozieAgents.add( agent );
        }


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

        po.addLog( String.format( "Installing Oozie server on %s...", config.getServer() ) );
        String sserver = Commands.make( CommandType.INSTALL_SERVER );
        Agent serverAgent = oozieManager.getAgentManager().getAgentByHostname( config.getServer() );
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

            Set<Agent> clients = new HashSet<>();
            for ( String clientHostname : config.getClients() )
            {
                Agent clientAgent = oozieManager.getAgentManager().getAgentByHostname( clientHostname );
                clients.add( clientAgent );
            }
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

        return config;
    }
}
