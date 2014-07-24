package org.safehaus.subutai.plugin.hadoop.impl;


import java.util.Set;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.impl.operation.common.InstallHadoopOperation;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

import com.google.common.base.Strings;


/**
 * This is a mongodb cluster setup strategy.
 */
public class HadoopDbSetupStrategy implements ClusterSetupStrategy {

    private Hadoop hadoopManager;
    private ContainerManager containerManager;
    private ProductOperation po;
    private HadoopClusterConfig hadoopClusterConfig;


    /*@todo add parameter validation logic*/
    public HadoopDbSetupStrategy( ProductOperation po, Hadoop hadoopManager, ContainerManager containerManager,
                                  HadoopClusterConfig hadoopClusterConfig ) {
        this.hadoopManager = hadoopManager;
        this.containerManager = containerManager;
        this.po = po;
        this.hadoopClusterConfig = hadoopClusterConfig;
    }


    public static PlacementStrategyENUM getNodePlacementStrategyByNodeType( NodeType nodeType ) {
        switch ( nodeType ) {
            case MASTER_NODE:
                return PlacementStrategyENUM.MORE_RAM;
            case SLAVE_NODE:
                return PlacementStrategyENUM.MORE_HDD;
            default:
                return PlacementStrategyENUM.ROUND_ROBIN;
        }
    }


    private void setMasterNodes( Set<Agent> agents ) {
        if ( agents != null && agents.size() >= 3 ) {
            Agent[] arr = agents.toArray( new Agent[agents.size()] );
            hadoopClusterConfig.setNameNode( arr[0] );
            hadoopClusterConfig.setJobTracker( arr[1] );
            hadoopClusterConfig.setSecondaryNameNode( arr[2] );
        }
    }


    private void setSlaveNodes( Set<Agent> agents ) {
        if ( agents != null ) {
            hadoopClusterConfig.getDataNodes().addAll( agents );
            hadoopClusterConfig.getTaskTrackers().addAll( agents );
        }
    }


    private void destroyLXC( ProductOperation po, String log ) {
        //destroy all lxcs also
        po.addLog( "Destroying lxc containers" );
        try {
            for ( Agent agent : hadoopClusterConfig.getAllNodes() ) {
                HadoopImpl.getContainerManager().cloneDestroy( agent.getParentHostName(), agent.getHostname() );
            }
            po.addLog( "Lxc containers successfully destroyed" );
        }
        catch ( LxcDestroyException ex ) {
            po.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
        }
        po.addLogFailed( log );
    }


    @Override
    public HadoopClusterConfig setup() throws ClusterSetupException {

        if ( hadoopClusterConfig == null ||
                Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ) ||
                Strings.isNullOrEmpty( hadoopClusterConfig.getDomainName() ) ) {
            po.addLogFailed( "Malformed configuration\nHadoop installation aborted" );
        }
        else {
            //check if mongo cluster with the same name already exists
            if ( hadoopManager.getCluster( hadoopClusterConfig.getClusterName() ) != null ) {
                po.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                        hadoopClusterConfig.getClusterName() ) );
            }
            else {
                try {
                    po.addLog( String.format( "Creating %d master servers...", 3 ) );
                    Set<Agent> cfgServers = containerManager.clone( hadoopClusterConfig.getTemplateName(), 3,
                            HadoopImpl.getAgentManager().getPhysicalAgents(),
                            getNodePlacementStrategyByNodeType( NodeType.MASTER_NODE ) );

                    po.addLog(
                            String.format( "Creating %d slave nodes...", hadoopClusterConfig.getCountOfSlaveNodes() ) );
                    Set<Agent> dataNodes = containerManager
                            .clone( hadoopClusterConfig.getTemplateName(), hadoopClusterConfig.getCountOfSlaveNodes(),
                                    HadoopImpl.getAgentManager().getPhysicalAgents(),
                                    getNodePlacementStrategyByNodeType( NodeType.SLAVE_NODE ) );

                    setMasterNodes( cfgServers );
                    setSlaveNodes( dataNodes );

                    po.addLog( "Lxc containers created successfully" );

                    //continue installation here

                    installHadoopCluster();

                    //@todo add containers destroyal in case of failure
                }
                catch ( LxcCreateException ex ) {
                    po.addLogFailed( ex.getMessage() );
                }
            }
        }

        return hadoopClusterConfig;
    }


    private void installHadoopCluster() throws ClusterSetupException {

        if ( HadoopImpl.getNetworkManager()
                       .configHostsOnAgents( hadoopClusterConfig.getAllNodes(), hadoopClusterConfig.getDomainName() )
                && HadoopImpl.getNetworkManager().configSshOnAgents( hadoopClusterConfig.getAllNodes() ) ) {
            po.addLog( "Cluster network configured" );

            po.addLog( "Hadoop installation started" );

            InstallHadoopOperation installOperation = new InstallHadoopOperation( hadoopClusterConfig );
            for ( Command command : installOperation.getCommandList() ) {
                po.addLog( ( String.format( "%s started...", command.getDescription() ) ) );
                HadoopImpl.getCommandRunner().runCommand( command );

                if ( command.hasSucceeded() ) {
                    po.addLogDone( String.format( "%s succeeded", command.getDescription() ) );
                }
                else {
                    po.addLogFailed(
                            String.format( "%s failed, %s", command.getDescription(), command.getAllErrors() ) );
                }
            }

            if ( HadoopImpl.getDbManager()
                           .saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
                                   hadoopClusterConfig ) ) {
                po.addLog( "Cluster info saved to DB" );
            }
            else {
                destroyLXC( po, "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
            }
        }
        else {
            destroyLXC( po, "Could not configure network! Please see logs\nLXC creation aborted" );
        }
    }
}
