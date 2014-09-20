package org.safehaus.subutai.plugin.oozie.impl.handler;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.SetupType;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;


/**
 * Created by bahadyr on 8/25/14.
 */
public class InstallHandler extends AbstractOperationHandler<OozieImpl>
{

    private ProductOperation po;
    private OozieClusterConfig config;
    private HadoopClusterConfig hadoopConfig;


    public InstallHandler( final OozieImpl manager, final OozieClusterConfig config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    @Override
    public void run()
    {
        ProductOperation po = productOperation;
        Environment env = null;

        if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {

            if ( hadoopConfig == null )
            {
                po.addLogFailed( "No Hadoop configuration specified" );
                return;
            }

            po.addLog( "Preparing environment..." );
            hadoopConfig.setTemplateName( OozieClusterConfig.PRODUCT_NAME_SERVER );
            try
            {
                EnvironmentBuildTask eb = manager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopConfig );
                env = manager.getEnvironmentManager().buildEnvironmentAndReturn( eb );
            }
            catch ( ClusterSetupException ex )
            {
                po.addLogFailed( "Failed to prepare environment: " + ex.getMessage() );
                return;
            }
            catch ( EnvironmentBuildException ex )
            {
                po.addLogFailed( "Failed to build environment: " + ex.getMessage() );
                return;
            }
            po.addLog( "Environment preparation completed" );
        }

        ClusterSetupStrategy s = manager.getClusterSetupStrategy( env, config, po );
        try
        {
            if ( s == null )
            {
                throw new ClusterSetupException( "No setup strategy" );
            }

            s.setup();
            po.addLogDone( "Done" );
        }
        catch ( ClusterSetupException ex )
        {
            po.addLogFailed( "Failed to setup cluster: " + ex.getMessage() );
        }


        /*final ProductOperation po =
            manager.getTracker().createProductOperation( OozieConfig.PRODUCT_KEY, "Installing Oozie" );

        manager.getExecutor().execute( new Runnable()
        {

            public void run()
            {
                if ( manager.getDbManager().getInfo( config.PRODUCT_KEY, config.getClusterName(), OozieConfig.class )
                    != null )
                {
                    po.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                        config.getClusterName() ) );
                    return;
                }

                for ( String node : config.getHadoopNodes() )
                {
                    if ( manager.getAgentManager().getAgentByHostname( node ) == null )
                    {
                        po.addLogFailed( String.format( "Node %s not connected\nAborted", node ) );
                        return;
                    }
                }

                if ( manager.getDbManager().saveInfo( config.PRODUCT_KEY, config.getClusterName(), config ) )
                {
                    po.addLog( "Cluster info saved to DB" );

                    //                    Set<Agent> allNodes = new HashSet<Agent>();
                    //                    allNodes.add(config.getServer());
                    //                    allNodes.addAll(config.getClients());

                    // Installing Oozie server
                    po.addLog( "Installing Oozie server..." );
                    Set<Agent> servers = new HashSet<Agent>();
                    Agent serverAgent = manager.getAgentManager().getAgentByHostname( config.getServer() );
                    servers.add( serverAgent );
                    Command installServerCommand = Commands.getInstallServerCommand( servers );
                    manager.getCommandRunner().runCommand( installServerCommand );

                    if ( installServerCommand.hasSucceeded() )
                    {
                        po.addLog( "Install server successful." );
                    }
                    else
                    {
                        po.addLogFailed(
                            String.format( "Installation failed, %s", installServerCommand.getAllErrors() ) );
                        return;
                    }

                    // Installing Oozie client
                    po.addLog( "Installing Oozie clients..." );
                    Set<Agent> clientAgents = new HashSet<Agent>();
                    for ( String clientAgent : config.getClients() )
                    {
                        Agent client = manager.getAgentManager().getAgentByHostname( clientAgent );
                        clientAgents.add( client );
                    }
                    Command installClientsCommand = Commands.getInstallClientCommand( clientAgents );
                    manager.getCommandRunner().runCommand( installClientsCommand );

                    if ( installClientsCommand.hasSucceeded() )
                    {
                        po.addLog( "Install clients successful." );
                    }
                    else
                    {
                        po.addLogFailed(
                            String.format( "Installation failed, %s", installClientsCommand.getAllErrors() ) );
                        return;
                    }

                    po.addLog( "Configuring root hosts..." );
                    Agent server = manager.getAgentManager().getAgentByHostname( config.getServer() );
                    Set<Agent> hadoopNodes = new HashSet<Agent>();
                    for ( String hadoopNode : config.getHadoopNodes() )
                    {
                        Agent hadoopNodeAgent = manager.getAgentManager().getAgentByHostname( hadoopNode );
                        hadoopNodes.add( hadoopNodeAgent );
                    }
                    Command configureRootHostsCommand = Commands.getConfigureRootHostsCommand( hadoopNodes,
                        AgentUtil.getAgentIpByMask( server, Common.IP_MASK ) );
                    manager.getCommandRunner().runCommand( configureRootHostsCommand );

                    if ( configureRootHostsCommand.hasSucceeded() )
                    {
                        po.addLog( "Configuring root hosts successful." );
                    }
                    else
                    {
                        po.addLogFailed(
                            String.format( "Configuration failed, %s", configureRootHostsCommand.getAllErrors() ) );
                        return;
                    }

                    po.addLog( "Configuring root groups..." );
                    Command configureRootGroupsCommand = Commands.getConfigureRootGroupsCommand( hadoopNodes );
                    manager.getCommandRunner().runCommand( configureRootGroupsCommand );

                    if ( configureRootGroupsCommand.hasSucceeded() )
                    {
                        po.addLog( "Configuring root groups successful." );
                    }
                    else
                    {
                        po.addLogFailed(
                            String.format( "Configuring failed, %s", configureRootGroupsCommand.getAllErrors() ) );
                        return;
                    }
                    po.addLogDone( "Oozie installation succeeded" );
                }
                else
                {
                    po.addLogFailed( "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
                }
            }
        } );*/
    }
}
