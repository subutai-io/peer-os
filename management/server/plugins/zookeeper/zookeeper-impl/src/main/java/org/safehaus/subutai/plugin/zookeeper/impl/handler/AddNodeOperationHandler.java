package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperStandaloneSetupStrategy;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.settings.Common;

import com.google.common.collect.Sets;


/**
 * Adds a node to ZK cluster. Install over a newly created lxc or over an existing hadoop cluster node
 */
public class AddNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;
    private String lxcHostname;


    public AddNodeOperationHandler( ZookeeperImpl manager, String clusterName ) {
        super( manager, clusterName );
        po = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
    }


    public AddNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Adding node to %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        final ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
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
            po.addLogFailed( String.format( "Node %s is not connected", lxcHostname ) );
            return;
        }

        po.addLog( "Preparing for node addition..." );

        //check installed subutai packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( Sets.newHashSet( lxcAgent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            po.addLogFailed( "Failed to check presence of installed subutai packages" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( lxcAgent.getUuid() );

        boolean hasZkInstalled =
                result.getStdOut().contains( Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_NAME );

        if ( hasZkInstalled ) {
            po.addLog( "Checking prerequisites..." );

            if ( !result.getStdOut().contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) ) {
                po.addLogFailed( String.format( "Node %s has no Hadoop installed", lxcHostname ) );
                return;
            }

            if ( config.getNodes().contains( lxcAgent ) ) {
                po.addLogFailed( String.format( "Agent with hostname %s already belongs to cluster %s", lxcHostname,
                        clusterName ) );
                return;
            }

            HadoopClusterConfig hadoopClusterConfig =
                    manager.getHadoopManager().getCluster( config.getHadoopClusterName() );

            if ( hadoopClusterConfig == null ) {
                po.addLogFailed( String.format( "Hadoop cluster %s not found", config.getHadoopClusterName() ) );
                return;
            }

            if ( !hadoopClusterConfig.getAllNodes().contains( lxcAgent ) ) {
                po.addLogFailed( String.format( "Specified node does not belong to Hadoop cluster %s",
                        config.getHadoopClusterName() ) );
                return;
            }

            config.getNodes().add( lxcAgent );
            config.setNumberOfNodes( config.getNumberOfNodes() + 1 );

            try {
                new ClusterConfiguration( manager, po ).configureCluster( config );
            }
            catch ( ClusterConfigurationException e ) {
                po.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
                return;
            }

            //update db
            po.addLog( "Updating cluster information in database..." );

            try {
                manager.getPluginDAO().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
                po.addLogDone( "Cluster information updated in database" );
            }
            catch ( DBException e ) {
                po.addLogFailed(
                        String.format( "Error while updating cluster information in database, %s", e.getMessage() ) );
            }
        }
        else {
            addOverHadoop( config );
        }
    }


    private void addOverHadoop( final ZookeeperClusterConfig config ) {

        //check if node agent is connected
        Agent lxcAgent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( lxcAgent == null ) {
            po.addLogFailed( String.format( "Node %s is not connected", lxcHostname ) );
            return;
        }

        if ( config.getNodes().contains( lxcAgent ) ) {
            po.addLogFailed(
                    String.format( "Agent with hostname %s already belongs to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        HadoopClusterConfig hadoopClusterConfig =
                manager.getHadoopManager().getCluster( config.getHadoopClusterName() );

        if ( hadoopClusterConfig == null ) {
            po.addLogFailed( String.format( "Hadoop cluster %s not found", config.getHadoopClusterName() ) );
            return;
        }

        if ( !hadoopClusterConfig.getAllNodes().contains( lxcAgent ) ) {
            po.addLogFailed( String.format( "Specified node does not belong to Hadoop cluster %s",
                    config.getHadoopClusterName() ) );
            return;
        }

        po.addLog( "Checking prerequisites..." );

        //check installed subutai packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( Sets.newHashSet( lxcAgent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            po.addLogFailed( "Failed to check presence of installed subutai packages" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( lxcAgent.getUuid() );

        if ( result.getStdOut().contains( Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_NAME ) ) {
            po.addLogFailed( String.format( "Node %s already has Zookeeper installed", lxcHostname ) );
            return;
        }
        else if ( !result.getStdOut().contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) ) {
            po.addLogFailed( String.format( "Node %s has no Hadoop installed", lxcHostname ) );
            return;
        }


        config.getNodes().add( lxcAgent );
        config.setNumberOfNodes( config.getNumberOfNodes() + 1 );

        po.addLog( String.format( "Installing %s...", ZookeeperClusterConfig.PRODUCT_NAME ) );

        //install
        Command installCommand = Commands.getInstallCommand( Sets.newHashSet( lxcAgent ) );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasCompleted() ) {
            po.addLog( "Installation succeeded\nReconfiguring cluster..." );

            try {
                new ClusterConfiguration( manager, po ).configureCluster( config );
            }
            catch ( ClusterConfigurationException e ) {
                po.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
                return;
            }

            //update db
            po.addLog( "Updating cluster information in database..." );

            try {
                manager.getPluginDAO().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
                po.addLogDone( "Cluster information updated in database" );
            }
            catch ( DBException e ) {
                po.addLogFailed(
                        String.format( "Error while updating cluster information in database, %s", e.getMessage() ) );
            }
        }
        else {
            po.addLogFailed( String.format( "Installation failed, %s\nUse Terminal Module to cleanup",
                    installCommand.getAllErrors() ) );
        }
    }


    private void addStandalone( final ZookeeperClusterConfig config ) {
        try {

            //create lxc
            po.addLog( "Creating lxc container..." );

            Set<Agent> agents = manager.getContainerManager().clone( config.getTemplateName(), 1, null,
                    ZookeeperStandaloneSetupStrategy.getNodePlacementStrategy() );

            Agent agent = agents.iterator().next();

            po.addLog( "Lxc container created successfully" );

            config.getNodes().add( agent );
            config.setNumberOfNodes( config.getNumberOfNodes() + 1 );

            //reconfigure cluster
            try {
                new ClusterConfiguration( manager, po ).configureCluster( config );
            }
            catch ( ClusterConfigurationException e ) {
                po.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
                return;
            }

            //update db
            po.addLog( "Updating cluster information in database..." );

            try {
                manager.getPluginDAO().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
                po.addLogDone( "Cluster information updated in database" );
            }
            catch ( DBException e ) {
                po.addLogFailed(
                        String.format( "Error while updating cluster information in database, %s", e.getMessage() ) );
            }
        }
        catch ( LxcCreateException ex ) {
            po.addLogFailed( ex.getMessage() );
        }
    }
}
