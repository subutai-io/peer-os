package org.safehaus.subutai.impl.mongodb;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.api.mongodb.Config;
import org.safehaus.subutai.api.mongodb.Mongo;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.ConfigBase;


/**
 * Created by dilshat on 7/21/14.
 */
public class MongoDbSetupStrategy implements ClusterSetupStrategy {

    private Mongo mongoManager;
    private ContainerManager containerManager;
    private AgentManager agentManager;
    private ProductOperation po;
    private Config config;
    private static final String templateName = "mongodb";


    /*@todo add parameter validation logic*/
    public MongoDbSetupStrategy( AgentManager agentManager, ProductOperation po, Mongo mongoManager,
                                 ContainerManager containerManager, final String clusterName,
                                 final String replicaSetName, final String domainName, final int numberOfConfigServers,
                                 final int numberOfRouters, final int numberOfDataNodes, final int cfgSrvPort,
                                 final int routerPort, final int dataNodePort ) {

        this.mongoManager = mongoManager;
        this.containerManager = containerManager;
        this.agentManager = agentManager;
        this.po = po;
        config = new Config();

        config.setCfgSrvPort( cfgSrvPort );
        config.setRouterPort( routerPort );
        config.setDataNodePort( dataNodePort );
        config.setClusterName( clusterName );
        config.setReplicaSetName( replicaSetName );
        config.setDomainName( domainName );
        config.setNumberOfDataNodes( numberOfDataNodes );
        config.setNumberOfConfigServers( numberOfConfigServers );
        config.setNumberOfRouters( numberOfRouters );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {


        //check if mongo cluster with the same name already exists
        if ( mongoManager.getCluster( config.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                            config.getClusterName() ) );
        }

        try {
            int numberOfLxcsNeeded =
                    config.getNumberOfConfigServers() + config.getNumberOfRouters() + config.getNumberOfDataNodes();
            //clone lxc containers
            po.addLog( String.format( "Creating %d config servers...", config.getNumberOfConfigServers() ) );
            Set<Agent> cfgServers = containerManager
                    .clone( UUID.randomUUID(), templateName, config.getNumberOfConfigServers(),
                            agentManager.getPhysicalAgents(), PlacementStrategyENUM.MORE_CPU );

            po.addLog( String.format( "Creating %d routers...", config.getNumberOfRouters() ) );
            Set<Agent> routers = containerManager.clone( UUID.randomUUID(), templateName, config.getNumberOfRouters(),
                    agentManager.getPhysicalAgents(), PlacementStrategyENUM.MORE_RAM );

            po.addLog( String.format( "Creating %d data nodes...", config.getNumberOfDataNodes() ) );
            Set<Agent> dataNodes = containerManager
                    .clone( UUID.randomUUID(), templateName, config.getNumberOfDataNodes(),
                            agentManager.getPhysicalAgents(), PlacementStrategyENUM.MORE_HDD );


            config.setConfigServers( cfgServers );
            config.setRouterServers( routers );
            config.setDataNodes( dataNodes );

            po.addLog( "Lxc containers created successfully" );

            //continue installation here

        }
        catch ( LxcCreateException ex ) {
            throw new ClusterSetupException( ex.getMessage() );
        }


        return null;
    }
}
