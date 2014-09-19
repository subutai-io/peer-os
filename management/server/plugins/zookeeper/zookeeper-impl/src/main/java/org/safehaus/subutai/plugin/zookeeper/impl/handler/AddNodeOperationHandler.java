package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import com.datastax.driver.core.Cluster;
import com.google.common.collect.Sets;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperStandaloneSetupStrategy;

import java.util.Set;
import java.util.UUID;


/**
 * Adds a node to ZK cluster. Install over a newly created lxc or over an existing hadoop cluster node
 */
public class AddNodeOperationHandler  extends AbstractOperationHandler<ZookeeperImpl> {

    private String lxcHostname;
    private ClusterConfiguration clusterConfiguration;

    public AddNodeOperationHandler( ZookeeperImpl manager, String clusterName ) {
        super( manager, clusterName );
        clusterConfiguration = new ClusterConfiguration( manager, productOperation );
        productOperation = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
    }


    public AddNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        clusterConfiguration = new ClusterConfiguration( manager, productOperation );
        productOperation = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return productOperation.getId();
    }


    @Override
    public void run() {
        final ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        if ( config.getSetupType() == SetupType.STANDALONE ) {
            addStandalone( config );
        }
        else if ( config.getSetupType() == SetupType.OVER_HADOOP ) {
            addOverHadoop( config );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP ) {
            addWithHadoop( config );
        }
    }


    private void addWithHadoop( final ZookeeperClusterConfig config ) {

        //check if node agent is connected
        Agent lxcAgent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( lxcAgent == null ) {
            productOperation.addLogFailed( String.format( "Node %s is not connected", lxcHostname ) );
            return;
        }

        productOperation.addLog( "Preparing for node addition..." );

        //check installed subutai packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( Sets.newHashSet( lxcAgent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            productOperation.addLogFailed( "Failed to check presence of installed subutai packages" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( lxcAgent.getUuid() );

        boolean hasZkInstalled =
                result.getStdOut().contains( Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_NAME );

        if ( hasZkInstalled ) {
            productOperation.addLog( "Checking prerequisites..." );

            if ( !result.getStdOut().contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) ) {
                productOperation.addLogFailed( String.format( "Node %s has no Hadoop installed", lxcHostname ) );
                return;
            }

            if ( config.getNodes().contains( lxcAgent ) ) {
                productOperation.addLogFailed( String.format( "Agent with hostname %s already belongs to cluster %s", lxcHostname,
                        clusterName ) );
                return;
            }

            HadoopClusterConfig hadoopClusterConfig =
                    manager.getHadoopManager().getCluster( config.getHadoopClusterName() );

            if ( hadoopClusterConfig == null ) {
                productOperation.addLogFailed( String.format( "Hadoop cluster %s not found", config.getHadoopClusterName() ) );
                return;
            }

            if ( !hadoopClusterConfig.getAllNodes().contains( lxcAgent ) ) {
                productOperation.addLogFailed( String.format( "Specified node does not belong to Hadoop cluster %s",
                        config.getHadoopClusterName() ) );
                return;
            }

            config.getNodes().add( lxcAgent );
            config.setNumberOfNodes( config.getNumberOfNodes() + 1 );

            try {
                new ClusterConfiguration( manager, productOperation ).configureCluster( config );
            }
            catch ( ClusterConfigurationException e ) {
                productOperation.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
                return;
            }

            //update db
            productOperation.addLog( "Updating cluster information in database..." );

            manager.getPluginDAO().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            productOperation.addLogDone( "Cluster information updated in database" );
        }
        else {
            addOverHadoop( config );
        }
    }


    private void addOverHadoop( final ZookeeperClusterConfig config ) {

        //check if node agent is connected
        Agent lxcAgent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( lxcAgent == null ) {
            productOperation.addLogFailed( String.format( "Node %s is not connected", lxcHostname ) );
            return;
        }

        if ( config.getNodes().contains( lxcAgent ) ) {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s already belongs to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        HadoopClusterConfig hadoopClusterConfig =
                manager.getHadoopManager().getCluster( config.getHadoopClusterName() );

        if ( hadoopClusterConfig == null ) {
            productOperation.addLogFailed( String.format( "Hadoop cluster %s not found", config.getHadoopClusterName() ) );
            return;
        }

        if ( !hadoopClusterConfig.getAllNodes().contains( lxcAgent ) ) {
            productOperation.addLogFailed( String.format( "Specified node does not belong to Hadoop cluster %s",
                    config.getHadoopClusterName() ) );
            return;
        }

        productOperation.addLog( "Checking prerequisites..." );

        //check installed subutai packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( Sets.newHashSet( lxcAgent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            productOperation.addLogFailed( "Failed to check presence of installed subutai packages" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( lxcAgent.getUuid() );

        if ( result.getStdOut().contains( Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_NAME ) ) {
            productOperation.addLogFailed( String.format( "Node %s already has Zookeeper installed", lxcHostname ) );
            return;
        }
        else if ( !result.getStdOut().contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) ) {
            productOperation.addLogFailed( String.format( "Node %s has no Hadoop installed", lxcHostname ) );
            return;
        }


        config.getNodes().add( lxcAgent );
        config.setNumberOfNodes( config.getNumberOfNodes() + 1 );

        productOperation.addLog( String.format( "Installing %s...", ZookeeperClusterConfig.PRODUCT_NAME ) );

        //install
        Command installCommand = Commands.getInstallCommand( Sets.newHashSet( lxcAgent ) );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasCompleted() ) {
            productOperation.addLog( "Installation succeeded\nReconfiguring cluster..." );

            try {
                new ClusterConfiguration( manager, productOperation ).configureCluster( config );
            }
            catch ( ClusterConfigurationException e ) {
                productOperation.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
                return;
            }

            //update db
            productOperation.addLog( "Updating cluster information in database..." );

            manager.getPluginDAO().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            productOperation.addLogDone( "Cluster information updated in database" );
        }
        else {
            productOperation.addLogFailed( String.format( "Installation failed, %s\nUse Terminal Module to cleanup",
                    installCommand.getAllErrors() ) );
        }
    }


    private void addStandalone( final ZookeeperClusterConfig config ) {
        try {

            //create lxc
            productOperation.addLog( "Creating lxc container..." );

            Set<Agent> agents = manager.getContainerManager().clone( config.getTemplateName(), 1, null,
                    ZookeeperStandaloneSetupStrategy.getNodePlacementStrategy() );

            Agent agent = agents.iterator().next();

            productOperation.addLog( "Lxc container created successfully" );

            config.getNodes().add( agent );
            config.setNumberOfNodes( config.getNumberOfNodes() + 1 );

            //reconfigure cluster
            try {
                getClusterConfiguration().configureCluster( config );
//                new ClusterConfiguration( manager, productOperation ).configureCluster( config );
            }
            catch ( ClusterConfigurationException e ) {
                productOperation.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
                return;
            }

            //update db
            productOperation.addLog( "Updating cluster information in database..." );

            manager.getPluginDAO().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            productOperation.addLogDone( "Cluster information updated in database" );
        }
        catch ( LxcCreateException ex ) {
            productOperation.addLogFailed( ex.getMessage() );
        }
    }

    public ClusterConfiguration getClusterConfiguration() {
        return clusterConfiguration;
    }

    public void setClusterConfiguration( ClusterConfiguration clusterConfiguration ) {
        this.clusterConfiguration = clusterConfiguration;
    }
}
