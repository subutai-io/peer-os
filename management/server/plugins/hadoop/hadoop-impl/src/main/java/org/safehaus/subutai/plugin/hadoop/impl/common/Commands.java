package org.safehaus.subutai.plugin.hadoop.impl.common;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public class Commands
{
    HadoopClusterConfig config;


    public Commands( HadoopClusterConfig config )
    {
        this.config = config;
    }


    public static String getStatusNameNodeCommand()
    {
        return "service hadoop-dfs status";
    }


    public static String getStartNameNodeCommand()
    {
        return "service hadoop-dfs start";
    }


    public static String getStopNameNodeCommand()
    {
        return "service hadoop-dfs stop";
    }


    public static String getStartJobTrackerCommand()
    {
        return "service hadoop-mapred start";
    }


    public static String getStopJobTrackerCommand()
    {
        return "service hadoop-mapred stop";
    }


    public static String getStatusJobTrackerCommand()
    {
        return "service hadoop-mapred status";
    }


    public static String getStatusDataNodeCommand()
    {
        return "service hadoop-dfs status";
    }


    public static String getClearMastersCommand()
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh masters clear";
    }


    public static String getClearSlavesCommand()
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh slaves clear";
    }


    public static String getAddSecondaryNamenodeCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh masters %s " + hadoopClusterConfig.getNameNode()
                                                                                                .getHostname();
    }


    public static String getSetDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, ContainerHost containerHost )
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh masters %s " + containerHost.getHostname();
    }


    public static String getRefreshJobTrackerCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return ". /etc/profile && " + "hadoop mradmin -refreshNodes";
    }


    public static String getStartDataNodeCommand()
    {
        return ". /etc/profile && " + "hadoop-daemons.sh start datanode";
    }


    public static String getStopDataNodeCommand()
    {
        return ". /etc/profile && " + "hadoop-daemons.sh stop datanode";
    }


    public static String getStartTaskTrackerCommand()
    {
        return ". /etc/profile && " + "hadoop-daemons.sh start tasktracker";
    }


    public static String getStopTaskTrackerCommand()
    {
        return ". /etc/profile && " + "hadoop-daemons.sh stop tasktracker";
    }


    public static String getStatusTaskTrackerCommand()
    {
        return ". /etc/profile && " + "hadoop-daemons.sh stop tasktracker";
    }


    public static String getFormatNameNodeCommand()
    {
        return ". /etc/profile && " + "hadoop namenode -format";
    }


    public static String getReportHadoopCommand()
    {
        return ". /etc/profile && " + "hadoop dfsadmin -report";
    }


    public String getConfigureJobTrackerCommand()
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh slaves " + config.getJobTracker().getHostname();
    }


    public String getConfigureSecondaryNameNodeCommand()
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh masters " + config.getSecondaryNameNode().getHostname();
    }


    public String getConfigureDataNodesCommand( String hostname )
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh slaves " + hostname;
    }


    public String getConfigureTaskTrackersCommand( String hostname )
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh slaves " + hostname;
    }


    public String getSetMastersCommand()
    {
        return ". /etc/profile && " + "hadoop-configure.sh " +
                config.getNameNode().getHostname() + ":" + HadoopClusterConfig.NAME_NODE_PORT + " " +
                config.getJobTracker().getHostname() + ":" + HadoopClusterConfig.JOB_TRACKER_PORT + " " +
                config.getReplicationFactor();
    }


    public String getRefreshNameNodeCommand( HadoopClusterConfig hadoopClusterConfig )
    {
        return ". /etc/profile && " + "hadoop dfsadmin -refreshNodes";
    }


    public String getStopDatanodeCommand( Agent agent )
    {
        return ". /etc/profile && " + "hadoop-daemons.sh stop datanode";
    }
}

//
//    public Command getNameNodeCommand( Agent agent, String command )
//    {
//        return commandRunner
//                .createCommand( String.format( "Execute NameNode/SecondaryNameNode/DataNode command %s",
// command ),
//                        new RequestBuilder( String.format( "service hadoop-dfs %s", command ) ).withTimeout( 20 ),
//                        Sets.newHashSet( agent ) );
//    }
//
//

//
//    public Command getSetDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
//    {
//
//        return commandRunner.createCommand( "Set DataNodes for NameNode", new RequestBuilder(
//                String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves %s; ",
//                        agent.getHostname() ) ), Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
//    }
//
//
//    public Command getSetTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig )
//    {
//
//        StringBuilder cmd = new StringBuilder();
//        for ( Agent agent : hadoopClusterConfig.getTaskTrackers() )
//        {
//            cmd.append(
//                    String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves %s; ",
// agent.getHostname() ) );
//        }
//
//        return commandRunner.createCommand( "Set TaskTrackers for JobTracker",
// new RequestBuilder( cmd.toString() ),
//                Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
//    }
//
//
//    public Command getSetTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
//    {
//
//        return commandRunner.createCommand( "Set TaskTrackers for JobTracker", new RequestBuilder(
//                String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves %s; ",
//                        agent.getHostname() ) ), Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
//    }
//
//
//    public Command getRemoveDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
//    {
//
//        return commandRunner.createCommand( "Remove DataNode from NameNode", new RequestBuilder(
//                String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves clear %s",
//                        agent.getHostname() ) ), Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
//    }
//
//
//    public Command getRemoveTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
//    {
//
//        return commandRunner.createCommand( "Remove TaskTrackers from JobTracker", new RequestBuilder(
//                String.format( ". /etc/profile && " + "hadoop-master-slave.sh slaves clear %s",
//                        agent.getHostname() ) ), Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
//    }
//
//
//    public Command getExcludeDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
//    {
//        return commandRunner.createCommand( "Remove DataNode from dfs blacklist", new RequestBuilder(
//                String.format( ". /etc/profile && " + "hadoop-master-slave.sh dfs.exclude clear %s",
//                        agent.getListIP().get( 0 ) ) ), Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
//    }
//
//
//    public Command getExcludeTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
//    {
//        return commandRunner.createCommand( "Remove TaskTracker from mapred blacklist", new RequestBuilder(
//                String.format( ". /etc/profile && " + "hadoop-master-slave.sh mapred.exclude clear %s",
//                        agent.getListIP().get( 0 ) ) ), Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
//    }
//
//
//    public Command getIncludeDataNodeCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
//    {
//        return commandRunner.createCommand( "Add DataNode to dfs blacklist", new RequestBuilder(
//                String.format( ". /etc/profile && " + "hadoop-master-slave.sh dfs.exclude %s",
//                        agent.getListIP().get( 0 ) ) ), Sets.newHashSet( hadoopClusterConfig.getNameNode() ) );
//    }
//
//
//    public Command getIncludeTaskTrackerCommand( HadoopClusterConfig hadoopClusterConfig, Agent agent )
//    {
//        return commandRunner.createCommand( "Add TaskTracker to mapred blacklist", new RequestBuilder(
//                String.format( ". /etc/profile && " + "hadoop-master-slave.sh mapred.exclude %s",
//                        agent.getListIP().get( 0 ) ) ), Sets.newHashSet( hadoopClusterConfig.getJobTracker() ) );
//    }
//

//    public String getJobTrackerCommand( Agent agent, String command )
//    {
//        return commandRunner.createCommand( String.format( "Execute JobTracker/TaskTracker command %s", command ),
//                new RequestBuilder( String.format( "service hadoop-mapred %s", command ) ).withTimeout( 20 ),
//                Sets.newHashSet( agent ) );
//    }
