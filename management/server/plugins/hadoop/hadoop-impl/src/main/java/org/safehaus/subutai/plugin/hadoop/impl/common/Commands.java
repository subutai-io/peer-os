package org.safehaus.subutai.plugin.hadoop.impl.common;


import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandsSingleton;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Created by daralbaev on 02.04.14.
 */
public class Commands extends CommandsSingleton
{

    public static Command getInstallCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return createCommand( "Installing hadoop deb package",
                new RequestBuilder( "sleep 10;" + "apt-get --force-yes --assume-yes install ksks-hadoop" )
                        .withTimeout( 180 ), Sets.newHashSet( hadoopClusterConfig.getAllNodes() ) );
    }


    public static Command getInstallCommand( Agent agent )
    {
        return createCommand( "Installing hadoop deb package",
                new RequestBuilder( "sleep 10;" + "apt-get --force-yes --assume-yes install ksks-hadoop" )
                        .withTimeout( 180 ), Sets.newHashSet( agent ) );
    }


    public static Command getClearMastersCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return createCommand( "Clear master nodes for NameNode",
                new RequestBuilder( ". /etc/profile && " + "hadoop-master-slave.sh masters clear" ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public static Command getClearSlavesCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return createCommand( "Clear slave nodes for NameNode and JobTracker",
                new RequestBuilder( ". /etc/profile && " + "hadoop-master-slave.sh slaves clear" ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode(), hadoopClusterConfig.getJobTracker() ) );
    }


    public static Command getSetMastersCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return createCommand( "Set masters for nodes",
                new RequestBuilder( ". /etc/profile && " + "hadoop-configure.sh" ).withCmdArgs( Lists.newArrayList(
                        String.format( "%s:%d", hadoopClusterConfig.getNameNode().getHostname(),
                                HadoopClusterConfig.NAME_NODE_PORT ),
                        String.format( "%s:%d", hadoopClusterConfig.getJobTracker().getHostname(),
                                HadoopClusterConfig.JOB_TRACKER_PORT ),
                        String.format( "%d", hadoopClusterConfig.getReplicationFactor() ) ) ),
                Sets.newHashSet( hadoopClusterConfig.getAllNodes() ) );
    }


    public static Command getSetMastersCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return createCommand( "Set masters for nodes",
                new RequestBuilder( ". /etc/profile && " + "hadoop-configure.sh" ).withCmdArgs( Lists.newArrayList(
                        String.format( "%s:%d", hadoopClusterConfig.getNameNode().getHostname(),
                                HadoopClusterConfig.NAME_NODE_PORT ),
                        String.format( "%s:%d", hadoopClusterConfig.getJobTracker().getHostname(),
                                HadoopClusterConfig.JOB_TRACKER_PORT ),
                        String.format( "%d", hadoopClusterConfig.getReplicationFactor() ) ) ),
                Sets.newHashSet( agent ) );
    }


    public static Command getAddSecondaryNamenodeCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return createCommand( "Set Secondary NameNode master for NameNode", new RequestBuilder(
                        String.format( ". /etc/profile && " + "hadoop-master-slave.sh masters %s",
                                hadoopClusterConfig.getSecondaryNameNode().getHostname() ) ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public static Command getSetDataNodeCommand( HadoopClusterConfig hadoopClusterConfig )
    {

        StringBuilder cmd = new StringBuilder();
        for ( Agent agent : hadoopClusterConfig.getDataNodes() )
        {
            cmd.append(
                    String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves %s; ", agent.getHostname() ) );
        }

        return createCommand( "Set DataNodes for NameNode", new RequestBuilder( cmd.toString() ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public static Command getSetDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {

        return createCommand( "Set DataNodes for NameNode", new RequestBuilder(
                        String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves %s; ", agent.getHostname() ) ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public static Command getSetTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig )
    {

        StringBuilder cmd = new StringBuilder();
        for ( Agent agent : hadoopClusterConfig.getTaskTrackers() )
        {
            cmd.append(
                    String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves %s; ", agent.getHostname() ) );
        }

        return createCommand( "Set TaskTrackers for JobTracker", new RequestBuilder( cmd.toString() ),
                Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
    }


    public static Command getSetTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {

        return createCommand( "Set TaskTrackers for JobTracker", new RequestBuilder(
                        String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves %s; ", agent.getHostname() ) ),
                Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
    }


    public static Command getRemoveDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {

        return createCommand( "Remove DataNode from NameNode", new RequestBuilder(
                        String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves clear %s",
                                agent.getHostname() ) ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public static Command getRemoveTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {

        return createCommand( "Remove TaskTrackers from JobTracker", new RequestBuilder(
                        String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves clear %s",
                                agent.getHostname() ) ),
                Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
    }


    public static Command getExcludeDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return createCommand( "Remove DataNode from dfs blacklist", new RequestBuilder(
                String.format( ". /etc/profile && " + "hadoop-master-slave.sh dfs.exclude clear %s",
                        agent.getListIP().get( 0 ) ) ), Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public static Command getExcludeTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return createCommand( "Remove TaskTracker from mapred blacklist", new RequestBuilder(
                String.format( ". /etc/profile && " + "hadoop-master-slave.sh mapred.exclude clear %s",
                        agent.getListIP().get( 0 ) ) ), Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
    }


    public static Command getIncludeDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return createCommand( "Add DataNode to dfs blacklist", new RequestBuilder(
                        String.format( ". /etc/profile && " + "hadoop-master-slave.sh dfs.exclude %s",
                                agent.getListIP().get( 0 ) ) ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public static Command getIncludeTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return createCommand( "Add TaskTracker to mapred blacklist", new RequestBuilder(
                String.format( ". /etc/profile && " + "hadoop-master-slave.sh mapred.exclude %s",
                        agent.getListIP().get( 0 ) ) ), Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
    }


    public static Command getFormatNameNodeCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return createCommand( "Format NameNode before first start",
                new RequestBuilder( ". /etc/profile && " + "hadoop namenode -format" ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public static Command getRefreshNameNodeCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return createCommand( "Refresh NameNode",
                new RequestBuilder( ". /etc/profile && " + "hadoop dfsadmin -refreshNodes" ).withTimeout( 5 ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public static Command getRefreshJobTrackerCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return createCommand( "Refresh JobTracker",
                new RequestBuilder( ". /etc/profile && " + "hadoop mradmin -refreshNodes" ).withTimeout( 5 ),
                Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
    }


    public static Command getStartDataNodeCommand( Agent agent ) {
        return createCommand( "Start DataNode",
                new RequestBuilder( ". /etc/profile && " + "hadoop-daemons.sh start datanode" ).withTimeout( 20 ),
                Sets.newHashSet( agent ) );
    }


    public static Command getStartTaskTrackerCommand( Agent agent )
    {
        return createCommand( "Start TaskTracker",
                new RequestBuilder( ". /etc/profile && " + "hadoop-daemons.sh start tasktracker" ).withTimeout( 20 ),
                Sets.newHashSet( agent ) );
    }


    public static Command getStopTaskTrackerCommand( Agent agent ) {
        return createCommand( "Stop TaskTracker",
                new RequestBuilder( ". /etc/profile && " + "hadoop-daemons.sh stop tasktracker" ).withTimeout( 20 ),
                Sets.newHashSet( agent ) );
    }


    public static Command getNameNodeCommand( Agent agent, String command ) {
        return createCommand( String.format( "Execute NameNode/SecondaryNameNode/DataNode command %s", command ),
                new RequestBuilder( String.format( "service hadoop-dfs %s", command ) ).withTimeout( 20 ),
                Sets.newHashSet( agent ) );
    }


    public static Command getReportHadoopCommand( HadoopClusterConfig hadoopClusterConfig ) {
        return createCommand( String.format( "Getting hadoop report" ),
                new RequestBuilder( String.format( ". /etc/profile && " + "hadoop dfsadmin -report" ) ).withTimeout( 20 ),
                Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
    }


    public static Command getStopDatanodeCommand( Agent agent ) {
        return createCommand( String.format( "Stop DataNode" ),
                new RequestBuilder( String.format( ". /etc/profile && " + "hadoop-daemons.sh stop datanode" ) ).withTimeout( 20 ),
                Sets.newHashSet( agent ) );
    }



    public static Command getJobTrackerCommand( Agent agent, String command ) {
        return createCommand( String.format( "Execute JobTracker/TaskTracker command %s", command ),
                new RequestBuilder( String.format( "service hadoop-mapred %s", command ) ).withTimeout( 20 ),
                Sets.newHashSet( agent ) );
    }


}
