package org.safehaus.subutai.plugin.zookeeper.impl;


import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.google.common.base.Strings;


/**
 * Configures ZK cluster
 */
public class ClusterConfiguration
{

    private ZookeeperImpl manager;
    private TrackerOperation po;


    public ClusterConfiguration( final ZookeeperImpl manager, final TrackerOperation po )
    {
        this.manager = manager;
        this.po = po;
    }


    public void configureCluster( final ZookeeperClusterConfig config ) throws ClusterConfigurationException
    {

        po.addLog( "Configuring cluster..." );

        Command configureClusterCommand;
        try
        {
            configureClusterCommand = manager.getCommands().getConfigureClusterCommand( config.getNodes(),
                    ConfigParams.DATA_DIR.getParamValue() + "/" + ConfigParams.MY_ID_FILE.getParamValue(),
                    prepareConfiguration( config.getNodes() ), ConfigParams.CONFIG_FILE_PATH.getParamValue() );
        }
        catch ( ClusterConfigurationException e )
        {
            throw new ClusterConfigurationException( String.format( "Error configuring cluster %s", e.getMessage() ) );
        }

        manager.getCommandRunner().runCommand( configureClusterCommand );

        if ( configureClusterCommand.hasSucceeded() )
        {
            po.addLog( "Cluster configured\nRestarting cluster..." );
            //restart all other nodes with new configuration
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
                po.addLog( "Cluster successfully restarted" );
            }
            else
            {
                po.addLog(
                        String.format( "Failed to restart cluster, %s, skipping...", restartCommand.getAllErrors() ) );
            }
        }
        else
        {

            throw new ClusterConfigurationException(
                    String.format( "Cluster configuration failed, %s", configureClusterCommand.getAllErrors() ) );
        }
    }


    //temporary workaround until we get full configuration injection working
    private String prepareConfiguration( Set<Agent> nodes ) throws ClusterConfigurationException
    {
        String zooCfgFile = FileUtil.getContent( "conf/zoo.cfg", ZookeeperStandaloneSetupStrategy.class );

        if ( Strings.isNullOrEmpty( zooCfgFile ) )
        {
            throw new ClusterConfigurationException( "Zoo.cfg resource is missing" );
        }

        zooCfgFile = zooCfgFile
                .replace( "$" + ConfigParams.DATA_DIR.getPlaceHolder(), ConfigParams.DATA_DIR.getParamValue() );

        /*
        server.1=zookeeper1:2888:3888
        server.2=zookeeper2:2888:3888
        server.3=zookeeper3:2888:3888
         */

        StringBuilder serversBuilder = new StringBuilder();
        int id = 0;
        for ( Agent agent : nodes )
        {
            serversBuilder.append( "server." ).append( ++id ).append( "=" ).append( agent.getHostname() )
                          .append( ConfigParams.PORTS.getParamValue() ).append( "\n" );
        }

        zooCfgFile = zooCfgFile.replace( "$" + ConfigParams.SERVERS.getPlaceHolder(), serversBuilder.toString() );


        return zooCfgFile;
    }
}
