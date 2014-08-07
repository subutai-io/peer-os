package org.safehaus.subutai.plugin.zookeeper.impl;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.FileUtil;
import org.safehaus.subutai.shared.protocol.PlacementStrategy;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This is a standalone zk cluster setup strategy.
 */
public class ZookeeperStandaloneSetupStrategy implements ClusterSetupStrategy {

    private final ZookeeperClusterConfig zookeeperClusterConfig;
    private final ZookeeperImpl zookeeperManager;
    private final ProductOperation po;
    private final Environment environment;


    public ZookeeperStandaloneSetupStrategy( final Environment environment,
                                             final ZookeeperClusterConfig zookeeperClusterConfig, ProductOperation po,
                                             ZookeeperImpl zookeeperManager ) {
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( zookeeperClusterConfig, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( zookeeperManager, "ZK manager is null" );

        this.zookeeperClusterConfig = zookeeperClusterConfig;
        this.po = po;
        this.zookeeperManager = zookeeperManager;
        this.environment = environment;
    }


    public static PlacementStrategy getNodePlacementStrategy() {
        return PlacementStrategy.ROUND_ROBIN;
    }


    @Override
    public ZookeeperClusterConfig setup() throws ClusterSetupException {
        if ( Strings.isNullOrEmpty( zookeeperClusterConfig.getClusterName() ) ||
                Strings.isNullOrEmpty( zookeeperClusterConfig.getTemplateName() ) ||
                zookeeperClusterConfig.getNumberOfNodes() <= 0 ) {
            throw new ClusterSetupException( "Malformed configuration" );
        }

        if ( zookeeperManager.getCluster( zookeeperClusterConfig.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", zookeeperClusterConfig.getClusterName() ) );
        }

        if ( environment.getNodes().size() < zookeeperClusterConfig.getNumberOfNodes() ) {
            throw new ClusterSetupException( String.format( "Environment needs to have %d nodes but has only %d nodes",
                    zookeeperClusterConfig.getNumberOfNodes(), environment.getNodes().size() ) );
        }

        Set<Agent> zkAgents = new HashSet<>();
        for ( Node node : environment.getNodes() ) {
            if ( node.getTemplate().getProducts()
                     .contains( Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_NAME ) ) {
                zkAgents.add( node.getAgent() );
            }
        }

        if ( zkAgents.size() < zookeeperClusterConfig.getNumberOfNodes() ) {
            throw new ClusterSetupException( String.format(
                    "Environment needs to have %d nodes with ZK installed but has only %d nodes with Zk installed",
                    zookeeperClusterConfig.getNumberOfNodes(), zkAgents.size() ) );
        }

        zookeeperClusterConfig.setNodes( zkAgents );

        //check if node agent is connected
        for ( Agent node : zookeeperClusterConfig.getNodes() ) {
            if ( zookeeperManager.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                throw new ClusterSetupException( String.format( "Node %s is not connected", node.getHostname() ) );
            }
        }

        try {

            configureZkCluster();
        }
        catch ( ClusterConfigurationException ex ) {
            throw new ClusterSetupException( ex.getMessage() );
        }

        po.addLog( "Saving cluster information to database..." );

        if ( zookeeperManager.getDbManager()
                             .saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, zookeeperClusterConfig.getClusterName(),
                                     zookeeperClusterConfig ) ) {
            po.addLog( "Cluster information saved to database" );
        }
        else {
            throw new ClusterSetupException( "Failed to save cluster information to database. Check logs" );
        }


        return zookeeperClusterConfig;
    }


    private void configureZkCluster() throws ClusterConfigurationException {

        po.addLog( "Configuring cluster..." );

        Command configureClusterCommand = Commands.getConfigureClusterCommand( zookeeperClusterConfig.getNodes(),
                ConfigParams.DATA_DIR.getParamValue() + "/" + ConfigParams.MY_ID_FILE.getParamValue(),
                prepareConfiguration( zookeeperClusterConfig.getNodes() ),
                ConfigParams.CONFIG_FILE_PATH.getParamValue() );

        zookeeperManager.getCommandRunner().runCommand( configureClusterCommand );

        if ( configureClusterCommand.hasSucceeded() ) {

            po.addLog( String.format( "Cluster configured\nStarting %s...", ZookeeperClusterConfig.PRODUCT_KEY ) );
            //start all nodes
            Command startCommand = Commands.getStartCommand( zookeeperClusterConfig.getNodes() );
            final AtomicInteger count = new AtomicInteger();
            zookeeperManager.getCommandRunner().runCommand( startCommand, new CommandCallback() {
                @Override
                public void onResponse( Response response, AgentResult agentResult, Command command ) {
                    if ( agentResult.getStdOut().contains( "STARTED" ) ) {
                        if ( count.incrementAndGet() == zookeeperClusterConfig.getNodes().size() ) {
                            stop();
                        }
                    }
                }
            } );

            if ( count.get() == zookeeperClusterConfig.getNodes().size() ) {
                po.addLog( String.format( "Starting %s succeeded", ZookeeperClusterConfig.PRODUCT_KEY ) );
            }
            else {
                po.addLog( String.format( "Starting %s failed, %s, skipping...", ZookeeperClusterConfig.PRODUCT_KEY,
                        startCommand.getAllErrors() ) );
            }
        }
        else {
            throw new ClusterConfigurationException(
                    String.format( "Failed to configure cluster, %s", configureClusterCommand.getAllErrors() ) );
        }
    }


    //temporary workaround until we get full configuration injection working
    public static String prepareConfiguration( Set<Agent> nodes ) throws ClusterConfigurationException {
        String zooCfgFile = FileUtil.getContent( "conf/zoo.cfg", ZookeeperStandaloneSetupStrategy.class );

        if ( Strings.isNullOrEmpty( zooCfgFile ) ) {
            throw new ClusterConfigurationException( "Zoo.cfg resource is missing" );
        }

        zooCfgFile = zooCfgFile
                .replace( "$" + ConfigParams.DATA_DIR.getPlaceHolder(), ConfigParams.DATA_DIR.getParamValue() );

        /*
        1=zookeeper1:2888:3888
        2=zookeeper2:2888:3888
        3=zookeeper3:2888:3888
         */

        StringBuilder serversBuilder = new StringBuilder();
        int id = 0;
        for ( Agent agent : nodes ) {
            serversBuilder.append( ++id ).append( "=" ).append( agent.getHostname() )
                          .append( ConfigParams.PORTS.getParamValue() ).append( "\n" );
        }

        zooCfgFile = zooCfgFile.replace( "$" + ConfigParams.SERVERS.getPlaceHolder(), serversBuilder.toString() );


        return zooCfgFile;
    }
}
