package org.safehaus.subutai.plugin.hadoop.impl.common;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


/**
 * This is a hadoop cluster init strategy.
 */
public class HadoopSetupStrategy implements ClusterSetupStrategy
{

    private Environment environment;
    private HadoopImpl hadoopManager;
    private TrackerOperation po;
    private HadoopClusterConfig hadoopClusterConfig;


    public HadoopSetupStrategy( TrackerOperation po, HadoopImpl hadoopManager, HadoopClusterConfig hadoopClusterConfig )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Hadoop cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( hadoopManager, "Hadoop manager is null" );

        this.hadoopManager = hadoopManager;
        this.po = po;
        this.hadoopClusterConfig = hadoopClusterConfig;
    }


    public HadoopSetupStrategy( TrackerOperation po, HadoopImpl hadoopManager, HadoopClusterConfig hadoopClusterConfig,
                                Environment environment )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Hadoop cluster config is null" );
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( hadoopManager, "Hadoop manager is null" );

        this.hadoopManager = hadoopManager;
        this.po = po;
        this.hadoopClusterConfig = hadoopClusterConfig;
        this.environment = environment;
    }


    public static PlacementStrategy getNodePlacementStrategyByNodeType( NodeType nodeType )
    {
        switch ( nodeType )
        {
            case MASTER_NODE:
                return PlacementStrategy.MORE_RAM;
            case SLAVE_NODE:
                return PlacementStrategy.MORE_HDD;
            default:
                return PlacementStrategy.ROUND_ROBIN;
        }
    }


    @Override
    public HadoopClusterConfig setup() throws ClusterSetupException
    {

        try
        {
            po.addLog( String.format( "Creating %d servers...", hadoopClusterConfig.getCountOfSlaveNodes()
                    + HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY ) );

            if ( this.environment == null )
            {
                environment = hadoopManager.getEnvironmentManager().buildEnvironment(
                        hadoopManager.getDefaultEnvironmentBlueprint( hadoopClusterConfig ) );
            }

            setMasterNodes();
            setSlaveNodes();
            po.addLog( "Lxc containers created successfully" );

            //continue installation here
            installHadoopCluster( environment );

            po.addLogDone(
                    String.format( "Cluster '%s' \nInstallation finished", hadoopClusterConfig.getClusterName() ) );
        }
        catch ( EnvironmentBuildException e )
        {
            try
            {
                hadoopManager.getEnvironmentManager().destroyEnvironment( environment.getId() );
            }
            catch ( EnvironmentDestroyException destroyException )
            {
                po.addLogFailed( String.format( "Failed to destroy environment %s. \n%s ", environment, destroyException ) );
                destroyException.printStackTrace();
            }
        }

        return hadoopClusterConfig;
    }


    private void installHadoopCluster( Environment environment ) throws ClusterSetupException
    {

        po.addLog( "Hadoop installation started" );
        hadoopManager.getPluginDAO().saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
                hadoopClusterConfig );
        po.addLog( "Cluster info saved to DB" );

        InstallHadoopOperation installOperation =
                new InstallHadoopOperation( hadoopManager.getCommands(), hadoopClusterConfig );
        for ( Command command : installOperation.getCommandList() )
        {
            po.addLog( ( String.format( "%s started...", command.getDescription() ) ) );
            hadoopManager.getCommandRunner().runCommand( command );

            if ( command.hasSucceeded() )
            {
                po.addLog( String.format( "%s succeeded", command.getDescription() ) );
            }
            else
            {
                po.addLogFailed( String.format( "%s failed, %s", command.getDescription(), command.getAllErrors() ) );
            }
        }
    }


    //    private void destroyLXC( TrackerOperation po, String log )
    //    {
    //        //destroy all lxcs also
    //        po.addLog( "Destroying lxc containers" );
    //        try
    //        {
    //            for ( Agent agent : hadoopClusterConfig.getAllNodes() )
    //            {
    //                hadoopManager.getContainerManager().cloneDestroy( agent.getParentHostName(), agent.getHostname() );
    //            }
    //            po.addLog( "Lxc containers successfully destroyed" );
    //        }
    //        catch ( LxcDestroyException ex )
    //        {
    //            po.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
    //        }
    //        po.addLogFailed( log );
    //    }


    private void setMasterNodes() throws ClusterSetupException
    {
        Set<Agent> masterNodes = new HashSet<>();

        for ( ContainerHost environmentContainer : this.environment.getContainers() )
        {
            if ( NodeType.MASTER_NODE.name().equalsIgnoreCase( environmentContainer.getNodeGroupName() ) )
            {
                if ( environmentContainer.getTemplate().getProducts()
                                         .contains( Common.PACKAGE_PREFIX + hadoopClusterConfig.getTemplateName() ) )
                {
                    masterNodes.add( environmentContainer.getAgent() );
                }
            }
        }

        if ( masterNodes.size() != HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY )
        {
            throw new ClusterSetupException( String.format( "Hadoop master nodes must be %d in count",
                    HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY ) );
        }

        Iterator<Agent> masterIterator = masterNodes.iterator();
        hadoopClusterConfig.setNameNode( masterIterator.next() );
        hadoopClusterConfig.setSecondaryNameNode( masterIterator.next() );
        hadoopClusterConfig.setJobTracker( masterIterator.next() );
    }


    private void setSlaveNodes() throws ClusterSetupException
    {
        Set<Agent> slaveNodes = new HashSet<>();

        for ( EnvironmentContainer environmentContainer : environment.getContainers() )
        {
            if ( NodeType.SLAVE_NODE.name().equalsIgnoreCase( environmentContainer.getNodeGroupName() ) )
            {
                if ( environmentContainer.getTemplate().getProducts()
                                         .contains( Common.PACKAGE_PREFIX + hadoopClusterConfig.getTemplateName() ) )
                {
                    slaveNodes.add( environmentContainer.getAgent() );
                }
            }
        }

        if ( slaveNodes.isEmpty() )
        {
            throw new ClusterSetupException( "Hadoop slave nodes are empty" );
        }

        hadoopClusterConfig.setDataNodes( Lists.newArrayList( slaveNodes ) );
        hadoopClusterConfig.setTaskTrackers( Lists.newArrayList( slaveNodes ) );
    }
}
