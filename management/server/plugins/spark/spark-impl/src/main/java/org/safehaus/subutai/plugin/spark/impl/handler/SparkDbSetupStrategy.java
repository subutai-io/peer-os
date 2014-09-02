package org.safehaus.subutai.plugin.spark.impl.handler;


import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

import com.google.common.base.Preconditions;


/**
 * Created by talas on 9/2/14.
 */
public class SparkDbSetupStrategy implements ClusterSetupStrategy {

    private Environment environment;
    private ProductOperation po;
    private Spark sparkManager;
    private SparkClusterConfig sparkClusterConfig;


    public SparkDbSetupStrategy( final ProductOperation po, final Spark sparkManager,
                                 final SparkClusterConfig sparkClusterConfig ) {
        Preconditions.checkNotNull( sparkClusterConfig, "Spark cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( sparkManager, "Spark manager is null" );

        this.po = po;
        this.sparkManager = sparkManager;
        this.sparkClusterConfig = sparkClusterConfig;
    }


    public SparkDbSetupStrategy( Environment environment, ProductOperation po, Spark sparkManager,
                                 SparkClusterConfig sparkClusterConfig ) {
        this.environment = environment;
        this.po = po;
        this.sparkManager = sparkManager;
        this.sparkClusterConfig = sparkClusterConfig;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {
        check();
        install();
        return sparkClusterConfig;
    }


    private void check() throws ClusterSetupException {
        po.addLog( "Checking prerequisites..." );

        //check installed ksks packages
        Set<Agent> allNodes = sparkClusterConfig.getAllNodes();
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( allNodes );
        SparkImpl.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            throw new ClusterSetupException(
                    "Failed to check presence of installed ksks packages\nInstallation aborted" );
        }
        for ( Iterator<Agent> it = allNodes.iterator(); it.hasNext(); ) {
            Agent node = it.next();

            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );
            if ( result.getStdOut().contains( "ksks-spark" ) ) {
                po.addLog( String.format( "Node %s already has Spark installed. Omitting this node from installation",
                        node.getHostname() ) );
                sparkClusterConfig.getSlaveNodes().remove( node );
                it.remove();
            }
            else if ( !result.getStdOut().contains( "ksks-hadoop" ) ) {
                po.addLog( String.format( "Node %s has no Hadoop installation. Omitting this node from installation",
                        node.getHostname() ) );
                sparkClusterConfig.getSlaveNodes().remove( node );
                it.remove();
            }
        }

        if ( sparkClusterConfig.getSlaveNodes().isEmpty() ) {
            throw new ClusterSetupException( "No nodes eligible for installation\nInstallation aborted" );
        }
        if ( !allNodes.contains( sparkClusterConfig.getMasterNode() ) ) {
            throw new ClusterSetupException( "Master node was omitted\nInstallation aborted" );
        }
    }


    private void install() throws ClusterSetupException {
        po.addLog( "Updating db..." );
        //save to db
        try {
            SparkImpl.getPluginDAO().saveInfo( SparkClusterConfig.PRODUCT_KEY, sparkClusterConfig.getClusterName(),
                    sparkClusterConfig );
            po.addLog( "Cluster info saved to DB\nInstalling Spark..." );
            //install spark
            Command installCommand = Commands.getInstallCommand( sparkClusterConfig.getAllNodes() );
            SparkImpl.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() ) {
                po.addLog( "Installation succeeded\nSetting master IP..." );

                Command setMasterIPCommand = Commands.getSetMasterIPCommand( sparkClusterConfig.getMasterNode(),
                        sparkClusterConfig.getAllNodes() );
                SparkImpl.getCommandRunner().runCommand( setMasterIPCommand );

                if ( setMasterIPCommand.hasSucceeded() ) {
                    po.addLog( "Setting master IP succeeded\nRegistering slaves..." );

                    Command addSlavesCommand = Commands.getAddSlavesCommand( sparkClusterConfig.getSlaveNodes(),
                            sparkClusterConfig.getMasterNode() );
                    SparkImpl.getCommandRunner().runCommand( addSlavesCommand );

                    if ( addSlavesCommand.hasSucceeded() ) {
                        po.addLog( "Slaves successfully registered\nStarting cluster..." );

                        Command startNodesCommand = Commands.getStartAllCommand( sparkClusterConfig.getMasterNode() );
                        final AtomicInteger okCount = new AtomicInteger( 0 );
                        SparkImpl.getCommandRunner().runCommand( startNodesCommand, new CommandCallback() {

                            @Override
                            public void onResponse( Response response, AgentResult agentResult, Command command ) {
                                okCount.set(
                                        StringUtil.countNumberOfOccurences( agentResult.getStdOut(), "starting" ) );

                                if ( okCount.get() >= sparkClusterConfig.getAllNodes().size() ) {
                                    stop();
                                }
                            }
                        } );

                        if ( okCount.get() >= sparkClusterConfig.getAllNodes().size() ) {
                            po.addLogDone( "cluster started successfully\nDone" );
                        }
                        else {
                            throw new ClusterSetupException(
                                    String.format( "Failed to start cluster, %s", startNodesCommand.getAllErrors() ) );
                        }
                    }
                    else {
                        throw new ClusterSetupException( String.format( "Failed to register slaves with master, %s",
                                addSlavesCommand.getAllErrors() ) );
                    }
                }
                else {
                    throw new ClusterSetupException(
                            String.format( "Setting master IP failed, %s", setMasterIPCommand.getAllErrors() ) );
                }
            }
            else {
                throw new ClusterSetupException(
                        String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
            }
        }
        catch ( DBException e ) {
            throw new ClusterSetupException(
                    "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
        }
    }
}
