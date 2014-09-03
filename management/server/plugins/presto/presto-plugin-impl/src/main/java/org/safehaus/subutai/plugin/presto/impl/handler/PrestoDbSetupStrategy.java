package org.safehaus.subutai.plugin.presto.impl.handler;


import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.CommandCallback;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.Commands;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;

import com.google.common.base.Preconditions;


public class PrestoDbSetupStrategy implements ClusterSetupStrategy {

    private Environment environment;
    private ProductOperation po;
    private Presto prestoManager;
    private PrestoClusterConfig prestoClusterConfig;


    public PrestoDbSetupStrategy( final ProductOperation po, final Presto prestoManager,
                                  final PrestoClusterConfig prestoClusterConfig ) {

        Preconditions.checkNotNull( prestoClusterConfig, "Presto cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( prestoManager, "Presto manager is null" );

        this.po = po;
        this.prestoManager = prestoManager;
        this.prestoClusterConfig = prestoClusterConfig;
    }


    public PrestoDbSetupStrategy( final Environment environment, final ProductOperation po,
                                  final PrestoImpl prestoManager, final PrestoClusterConfig prestoClusterConfig ) {
        Preconditions.checkNotNull( prestoClusterConfig, "Presto cluster config is null" );
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( prestoManager, "Presto manager is null" );

        this.environment = environment;
        this.po = po;
        this.prestoManager = prestoManager;
        this.prestoClusterConfig = prestoClusterConfig;
    }


    @Override
    public PrestoClusterConfig setup() throws ClusterSetupException {
        check();
        install();

        return prestoClusterConfig;
    }


    private void check() throws ClusterSetupException {
        po.addLog( "Checking prerequisites..." );
        //check installed ksks packages
        Set<Agent> allNodes = prestoClusterConfig.getAllNodes();
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( allNodes );
        PrestoImpl.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            throw new ClusterSetupException(
                    "Failed to check presence of installed ksks packages\nInstallation aborted" );
        }

        for ( Iterator<Agent> it = allNodes.iterator(); it.hasNext(); ) {
            Agent node = it.next();
            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );
            if ( result.getStdOut().contains( "ksks-presto" ) ) {
                po.addLog( String.format( "Node %s already has Presto installed. Omitting this node from installation",
                        node.getHostname() ) );
                prestoClusterConfig.getWorkers().remove( node );
                it.remove();
            }
            else if ( !result.getStdOut().contains( "ksks-hadoop" ) ) {
                po.addLog( String.format( "Node %s has no Hadoop installation. Omitting this node from installation",
                        node.getHostname() ) );
                prestoClusterConfig.getWorkers().remove( node );
                it.remove();
            }
        }

        if ( prestoClusterConfig.getWorkers().isEmpty() ) {
            throw new ClusterSetupException( "No nodes eligible for installation\nInstallation aborted" );
        }
        if ( !allNodes.contains( prestoClusterConfig.getCoordinatorNode() ) ) {
            throw new ClusterSetupException( "Coordinator node was omitted\nInstallation aborted" );
        }
    }


    private void install() throws ClusterSetupException {
        po.addLog( "Updating db..." );
        //save to db
        try {
            PrestoImpl.getPluginDAO().saveInfo( PrestoClusterConfig.PRODUCT_KEY, prestoClusterConfig.getClusterName(),
                    prestoClusterConfig );

            po.addLog( "Cluster info saved to DB\nInstalling Presto..." );
            //install presto

            Command installCommand = Commands.getInstallCommand( prestoClusterConfig.getAllNodes() );
            PrestoImpl.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() ) {
                po.addLog( "Installation succeeded\nConfiguring coordinator..." );

                Command configureCoordinatorCommand =
                        Commands.getSetCoordinatorCommand( prestoClusterConfig.getCoordinatorNode() );
                PrestoImpl.getCommandRunner().runCommand( configureCoordinatorCommand );

                if ( configureCoordinatorCommand.hasSucceeded() ) {
                    po.addLog( "Coordinator configured successfully\nConfiguring workers..." );

                    Command configureWorkersCommand =
                            Commands.getSetWorkerCommand( prestoClusterConfig.getCoordinatorNode(),
                                    prestoClusterConfig.getWorkers() );
                    PrestoImpl.getCommandRunner().runCommand( configureWorkersCommand );

                    if ( configureWorkersCommand.hasSucceeded() ) {
                        po.addLog( "Workers configured successfully\nStarting Presto..." );

                        Command startNodesCommand = Commands.getStartCommand( prestoClusterConfig.getAllNodes() );
                        final AtomicInteger okCount = new AtomicInteger();
                        PrestoImpl.getCommandRunner().runCommand( startNodesCommand, new CommandCallback() {

                            @Override
                            public void onResponse( Response response, AgentResult agentResult, Command command ) {
                                if ( agentResult.getStdOut().contains( "Started" )
                                        && okCount.incrementAndGet() == prestoClusterConfig.getAllNodes().size() ) {
                                    stop();
                                }
                            }
                        } );

                        if ( okCount.get() == prestoClusterConfig.getAllNodes().size() ) {
                            po.addLogDone( "Presto started successfully\nDone" );
                        }
                        else {
                            throw new ClusterSetupException(
                                    String.format( "Failed to start Presto, %s", startNodesCommand.getAllErrors() ) );
                        }
                    }
                    else {
                        throw new ClusterSetupException( String.format( "Failed to configure workers, %s",
                                configureWorkersCommand.getAllErrors() ) );
                    }
                }
                else {
                    throw new ClusterSetupException( String.format( "Failed to configure coordinator, %s",
                            configureCoordinatorCommand.getAllErrors() ) );
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
