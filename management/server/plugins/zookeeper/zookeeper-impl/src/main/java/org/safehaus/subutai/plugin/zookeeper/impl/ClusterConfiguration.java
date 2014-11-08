package org.safehaus.subutai.plugin.zookeeper.impl;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CommandCallback;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
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

        Environment environment =
                manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );

        String configureClusterCommand;
        Iterator<ContainerHost> iterator = config.getNodes().iterator();

        int nodeNumber=0;
        List<CommandResult> commandsResultList = new ArrayList<>();
        while( iterator.hasNext() ) {
            ContainerHost zookeeperNode = environment.getContainerHostByUUID( iterator.next().getId() );
            configureClusterCommand = manager.getCommands().getConfigureClusterCommand(
                    prepareConfiguration( config.getNodes() ), ConfigParams.CONFIG_FILE_PATH.getParamValue(), ++nodeNumber );
            CommandResult commandResult = null;
            try
            {
                commandResult = zookeeperNode.execute( new RequestBuilder( configureClusterCommand ).withTimeout( 60 ) );
            }
            catch ( CommandException e )
            {
                po.addLogFailed("Could not run command " + configureClusterCommand + ": " + e);
                e.printStackTrace();
            }
            commandsResultList.add( commandResult );
        }

        boolean isSuccesful = true;
        for ( int i = 0 ; i < commandsResultList.size(); i++ ) {
            if ( ! commandsResultList.get( i ).hasSucceeded() )
            {
                isSuccesful = false;
            }
        }
        if ( isSuccesful )
        {
            po.addLog( "Cluster configured\nRestarting cluster..." );
            //restart all other nodes with new configuration
            String restartCommand = manager.getCommands().getRestartCommand( );
            final AtomicInteger count = new AtomicInteger();
            iterator = config.getNodes().iterator();
            commandsResultList = new ArrayList<>();
            while( iterator.hasNext() ) {
                ContainerHost zookeeperNode = environment.getContainerHostByUUID( iterator.next().getId() );
                configureClusterCommand = manager.getCommands().getConfigureClusterCommand(
                        prepareConfiguration( config.getNodes() ), ConfigParams.CONFIG_FILE_PATH.getParamValue(), ++nodeNumber );
                CommandResult commandResult = null;
                try
                {
                    commandResult = zookeeperNode.execute( new RequestBuilder( restartCommand ).withTimeout( 60 ) );
                }
                catch ( CommandException e )
                {
                    po.addLogFailed("Could not run command " + configureClusterCommand + ": " + e);
                    e.printStackTrace();
                }
                commandsResultList.add( commandResult );
            }

            if ( count.get() == config.getNodes().size() )
            {
                po.addLog( "Cluster successfully restarted" );
            }
            else
            {
                po.addLog( String.format( "Failed to restart cluster, skipping..." ) );
            }
        }
        else
        {

            throw new ClusterConfigurationException( String.format( "Cluster configuration failed" ) );
        }
    }


    //temporary workaround until we get full configuration injection working
    private String prepareConfiguration( Set<ContainerHost> nodes ) throws ClusterConfigurationException
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
        for ( ContainerHost agent : nodes )
        {
            serversBuilder.append( "server." ).append( ++id ).append( "=" ).append( agent.getHostname() )
                          .append( ConfigParams.PORTS.getParamValue() ).append( "\n" );
        }

        zooCfgFile = zooCfgFile.replace( "$" + ConfigParams.SERVERS.getPlaceHolder(), serversBuilder.toString() );


        return zooCfgFile;
    }
}
