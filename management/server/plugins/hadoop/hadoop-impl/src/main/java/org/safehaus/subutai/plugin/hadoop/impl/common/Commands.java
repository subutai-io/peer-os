package org.safehaus.subutai.plugin.hadoop.impl.common;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Created by daralbaev on 02.04.14.
 */
public class Commands
{
    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME.toLowerCase();
    private final Environment environment;

    public Commands( final Environment environment )
    {
        Preconditions.checkNotNull( environment, "Environment is null" );

        this.environment = environment;
    }


    //    public Command getInstallCommand( HadoopClusterConfig hadoopClusterConfig )
    //    {
    //        return commandRunner.createCommand( "Installing hadoop deb package",
    //                new RequestBuilder( "sleep 10;" + "apt-get --force-yes --assume-yes install " + PACKAGE_NAME )
    //                        .withTimeout( 180 ), Sets.newHashSet( hadoopClusterConfig.getAllNodes() ) );
    //    }


    //    public Command getInstallCommand( Agent agent )
    //    {
    //        return commandRunner.createCommand( "Installing hadoop deb package",
    //                new RequestBuilder( "sleep 10;" + "apt-get --force-yes --assume-yes install " + PACKAGE_NAME )
    //                        .withTimeout( 180 ), Sets.newHashSet( agent ) );
    //    }


    public Command getClearMastersCommand( HadoopClusterConfig hadoopClusterConfig )
    {

        return ( "Clear master nodes for NameNode",
        new RequestBuilder( ". /etc/profile && " + "hadoop-master-slave.sh masters clear" ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public Command getClearSlavesCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        UUID nameNode = hadoopClusterConfig.getNameNode();
        ContainerHost containerHost = environment.getContainerHostByUUID( nameNode );
        if ( containerHost != null ) {
            containerHost.execute( )
            return commandRunner.createCommand( "Clear slave nodes for NameNode and JobTracker",
                    new RequestBuilder( ". /etc/profile && " + "hadoop-master-slave.sh slaves clear" ),
                    Sets.newHashSet( nameNode, hadoopClusterConfig.getJobTracker() ) );
        }
        return null;

    }


    public Command getSetMastersCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return commandRunner.createCommand( "Set masters for nodes",
                new RequestBuilder( ". /etc/profile && " + "hadoop-configure.sh" ).withCmdArgs( Lists.newArrayList(
                        String.format( "%s:%d", hadoopClusterConfig.getNameNode().getHostname(),
                                HadoopClusterConfig.NAME_NODE_PORT ),
                        String.format( "%s:%d", hadoopClusterConfig.getJobTracker().getHostname(),
                                HadoopClusterConfig.JOB_TRACKER_PORT ),
                        String.format( "%d", hadoopClusterConfig.getReplicationFactor() ) ) ),
                Sets.newHashSet( hadoopClusterConfig.getAllNodes() ) );
    }


    public Command getSetMastersCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return commandRunner.createCommand( "Set masters for nodes",
                new RequestBuilder( ". /etc/profile && " + "hadoop-configure.sh" ).withCmdArgs( Lists.newArrayList(
                        String.format( "%s:%d", hadoopClusterConfig.getNameNode().getHostname(),
                                HadoopClusterConfig.NAME_NODE_PORT ),
                        String.format( "%s:%d", hadoopClusterConfig.getJobTracker().getHostname(),
                                HadoopClusterConfig.JOB_TRACKER_PORT ),
                        String.format( "%d", hadoopClusterConfig.getReplicationFactor() ) ) ),
                Sets.newHashSet( agent ) );
    }


    public Command getAddSecondaryNamenodeCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return commandRunner.createCommand( "Set Secondary NameNode master for NameNode", new RequestBuilder(
                        String.format( ". /etc/profile && " + "hadoop-master-slave.sh masters %s",
                                hadoopClusterConfig.getSecondaryNameNode().getHostname() ) ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public Command getSetDataNodeCommand( HadoopClusterConfig hadoopClusterConfig )
    {

        StringBuilder cmd = new StringBuilder();
        for ( Agent agent : hadoopClusterConfig.getDataNodes() )
        {
            cmd.append(
                    String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves %s; ", agent.getHostname() ) );
        }

        return commandRunner.createCommand( "Set DataNodes for NameNode", new RequestBuilder( cmd.toString() ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public Command getSetDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {

        return commandRunner.createCommand( "Set DataNodes for NameNode", new RequestBuilder(
                String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves %s; ",
                        agent.getHostname() ) ), Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public Command getSetTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig )
    {

        StringBuilder cmd = new StringBuilder();
        for ( Agent agent : hadoopClusterConfig.getTaskTrackers() )
        {
            cmd.append(
                    String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves %s; ", agent.getHostname() ) );
        }

        return commandRunner.createCommand( "Set TaskTrackers for JobTracker", new RequestBuilder( cmd.toString() ),
                Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
    }


    public Command getSetTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {

        return commandRunner.createCommand( "Set TaskTrackers for JobTracker", new RequestBuilder(
                String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves %s; ",
                        agent.getHostname() ) ), Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
    }


    public Command getRemoveDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {

        return commandRunner.createCommand( "Remove DataNode from NameNode", new RequestBuilder(
                String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves clear %s",
                        agent.getHostname() ) ), Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public Command getRemoveTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {

        return commandRunner.createCommand( "Remove TaskTrackers from JobTracker", new RequestBuilder(
                String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves clear %s",
                        agent.getHostname() ) ), Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
    }


    public Command getExcludeDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return commandRunner.createCommand( "Remove DataNode from dfs blacklist", new RequestBuilder(
                String.format( ". /etc/profile && " + "hadoop-master-slave.sh dfs.exclude clear %s",
                        agent.getListIP().get( 0 ) ) ), Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public Command getExcludeTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return commandRunner.createCommand( "Remove TaskTracker from mapred blacklist", new RequestBuilder(
                String.format( ". /etc/profile && " + "hadoop-master-slave.sh mapred.exclude clear %s",
                        agent.getListIP().get( 0 ) ) ), Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
    }


    public Command getIncludeDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return commandRunner.createCommand( "Add DataNode to dfs blacklist", new RequestBuilder(
                String.format( ". /etc/profile && " + "hadoop-master-slave.sh dfs.exclude %s",
                        agent.getListIP().get( 0 ) ) ), Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public Command getIncludeTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return commandRunner.createCommand( "Add TaskTracker to mapred blacklist", new RequestBuilder(
                String.format( ". /etc/profile && " + "hadoop-master-slave.sh mapred.exclude %s",
                        agent.getListIP().get( 0 ) ) ), Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
    }


    public Command getFormatNameNodeCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return commandRunner.createCommand( "Format NameNode before first start",
                new RequestBuilder( ". /etc/profile && " + "hadoop namenode -format" ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public Command getRefreshNameNodeCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return commandRunner.createCommand( "Refresh NameNode",
                new RequestBuilder( ". /etc/profile && " + "hadoop dfsadmin -refreshNodes" ).withTimeout( 5 ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public Command getRefreshJobTrackerCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return commandRunner.createCommand( "Refresh JobTracker",
                new RequestBuilder( ". /etc/profile && " + "hadoop mradmin -refreshNodes" ).withTimeout( 5 ),
                Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
    }


    public Command getStartDataNodeCommand( Agent agent )
    {
        return commandRunner.createCommand( "Start DataNode",
                new RequestBuilder( ". /etc/profile && " + "hadoop-daemons.sh start datanode" ).withTimeout( 20 ),
                Sets.newHashSet( agent ) );
    }


    public Command getStartTaskTrackerCommand( Agent agent )
    {
        return commandRunner.createCommand( "Start TaskTracker",
                new RequestBuilder( ". /etc/profile && " + "hadoop-daemons.sh start tasktracker" ).withTimeout( 20 ),
                Sets.newHashSet( agent ) );
    }


    public Command getStopTaskTrackerCommand( Agent agent )
    {
        return commandRunner.createCommand( "Stop TaskTracker",
                new RequestBuilder( ". /etc/profile && " + "hadoop-daemons.sh stop tasktracker" ).withTimeout( 20 ),
                Sets.newHashSet( agent ) );
    }


    public Command getNameNodeCommand( Agent agent, String command )
    {
        return commandRunner
                .createCommand( String.format( "Execute NameNode/SecondaryNameNode/DataNode command %s", command ),
                        new RequestBuilder( String.format( "service hadoop-dfs %s", command ) ).withTimeout( 20 ),
                        Sets.newHashSet( agent ) );
    }


    public Command getReportHadoopCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return commandRunner.createCommand( String.format( "Getting hadoop report" ),
                new RequestBuilder( String.format( ". /etc/profile && " + "hadoop dfsadmin -report" ) )
                        .withTimeout( 20 ), Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public Command getStopDatanodeCommand( Agent agent )
    {
        return commandRunner.createCommand( String.format( "Stop DataNode" ),
                new RequestBuilder( String.format( ". /etc/profile && " + "hadoop-daemons.sh stop datanode" ) )
                        .withTimeout( 20 ), Sets.newHashSet( agent ) );
    }


    public Command getJobTrackerCommand( Agent agent, String command )
    {
        return commandRunner.createCommand( String.format( "Execute JobTracker/TaskTracker command %s", command ),
                new RequestBuilder( String.format( "service hadoop-mapred %s", command ) ).withTimeout( 20 ),
                Sets.newHashSet( agent ) );
    }
}
