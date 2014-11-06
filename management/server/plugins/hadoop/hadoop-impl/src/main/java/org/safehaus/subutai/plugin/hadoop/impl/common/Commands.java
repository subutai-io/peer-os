package org.safehaus.subutai.plugin.hadoop.impl.common;


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


    public static String getRefreshJobTrackerCommand()
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
        return "service hadoop-mapred status";
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


    public static String getRefreshNameNodeCommand()
    {
        return ". /etc/profile && " + "hadoop dfsadmin -refreshNodes";
    }


    public static String getSetDataNodeCommand( String hostname )
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh slaves " + hostname;
    }


    public static String getExcludeDataNodeCommand( String ip )
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh dfs.exclude clear " + ip;
    }


    public static String getSetTaskTrackerCommand( String hostname )
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh slaves " + hostname;
    }


    public static String getExcludeTaskTrackerCommand( String ip )
    {

        return ". /etc/profile && " + "hadoop-master-slave.sh mapred.exclude clear " + ip;
    }


    public static String getRemoveTaskTrackerCommand( String hostname )
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh slaves clear " + hostname;
    }


    public static String getIncludeTaskTrackerCommand( String ip )
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh mapred.exclude " + ip;
    }


    public static String getRemoveDataNodeCommand( String hostname )
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh slaves clear " + hostname;
    }


    public static String getIncludeDataNodeCommand( String ip )
    {
        return ". /etc/profile && " + "hadoop-master-slave.sh mapred.exclude " + ip;
    }
}