package org.safehaus.subutai.plugin.hadoop.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class CommandsTest
{
    Commands commands;
    HadoopClusterConfig hadoopClusterConfig;

    @Before
    public void setUp()
    {
        hadoopClusterConfig = mock(HadoopClusterConfig.class);
        commands = new Commands(hadoopClusterConfig);
    }

    @Test
    public void testGetStatusNameNodeCommand()
    {
        assertEquals("service hadoop-dfs status", commands.getStatusNameNodeCommand());
        assertNotNull(commands.getStatusNameNodeCommand());
    }

    @Test
    public void testGetStartNameNodeCommand()
    {
        assertEquals("service hadoop-dfs start", commands.getStartNameNodeCommand());
        assertNotNull(commands.getStartNameNodeCommand());
    }

    @Test
    public void testGetStopNameNodeCommand()
    {
        assertEquals("service hadoop-dfs stop", commands.getStopNameNodeCommand());
        assertNotNull(commands.getStopNameNodeCommand());
    }

    @Test
    public void testGetStartJobTrackerCommand()
    {
        assertEquals("service hadoop-mapred start", commands.getStartJobTrackerCommand());
        assertNotNull(commands.getStartJobTrackerCommand());
    }

    @Test
    public void testGetStopJobTrackerCommand()
    {
        assertEquals("service hadoop-mapred stop", commands.getStopJobTrackerCommand());
        assertNotNull(commands.getStopJobTrackerCommand());
    }

    @Test
    public void testGetStatusJobTrackerCommand()
    {
        assertEquals("service hadoop-mapred status", commands.getStatusJobTrackerCommand());
        assertNotNull(commands.getStatusJobTrackerCommand());
    }

    @Test
    public void testGetStatusDataNodeCommand()
    {
        assertEquals("service hadoop-dfs status", commands.getStatusDataNodeCommand());
        assertNotNull(commands.getStatusDataNodeCommand());
    }

    @Test
    public void testGetClearMastersCommand()
    {
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh masters clear", commands.getClearMastersCommand());
        assertNotNull(commands.getClearMastersCommand());
    }

    @Test
    public void testGetClearSlavesCommand()
    {
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh slaves clear", commands.getClearSlavesCommand());
        assertNotNull(commands.getClearSlavesCommand());
    }

    @Test
    public void testGetRefreshJobTrackerCommand()
    {
        assertEquals(". /etc/profile && " + "hadoop mradmin -refreshNodes", commands.getRefreshJobTrackerCommand());
        assertNotNull(commands.getRefreshJobTrackerCommand());
    }

    @Test
    public void testGetStartDataNodeCommand()
    {
        assertEquals(". /etc/profile && " + "hadoop-daemons.sh start datanode", commands.getStartDataNodeCommand());
        assertNotNull(commands.getStartDataNodeCommand());
    }

    @Test
    public void testGetStopDataNodeCommand()
    {
        assertEquals(". /etc/profile && " + "hadoop-daemons.sh stop datanode", commands.getStopDataNodeCommand());
        assertNotNull(commands.getStopDataNodeCommand());
    }

    @Test
    public void testGetStartTaskTrackerCommand()
    {
        assertEquals(". /etc/profile && " + "hadoop-daemons.sh start tasktracker", commands
                .getStartTaskTrackerCommand());
        assertNotNull(commands.getStartTaskTrackerCommand());
    }

    @Test
    public void testGetStopTaskTrackerCommand()
    {
        assertEquals(". /etc/profile && " + "hadoop-daemons.sh stop tasktracker", commands.getStopTaskTrackerCommand());
        assertNotNull(commands.getStopTaskTrackerCommand());
    }

    @Test
    public void testGetStatusTaskTrackerCommand()
    {
        assertEquals("service hadoop-mapred status", commands.getStatusTaskTrackerCommand());
        assertNotNull(commands.getStatusTaskTrackerCommand());
    }

    @Test
    public void testGetFormatNameNodeCommand()
    {
        assertEquals(". /etc/profile && " + "hadoop namenode -format", commands.getFormatNameNodeCommand());
        assertNotNull(commands.getFormatNameNodeCommand());
    }

    @Test
    public void testGetReportHadoopCommand()
    {
        assertEquals(". /etc/profile && " + "hadoop dfsadmin -report", commands.getReportHadoopCommand());
        assertNotNull(commands.getReportHadoopCommand());
    }

    @Test
    public void testGetRefreshNameNodeCommand()
    {
        assertEquals(". /etc/profile && " + "hadoop dfsadmin -refreshNodes", commands.getRefreshNameNodeCommand());
        assertNotNull(commands.getRefreshNameNodeCommand());
    }

    @Test
    public void testGetSetDataNodeCommand()
    {
        String hostname = "test";
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh slaves " + hostname, commands
                .getSetDataNodeCommand(hostname));
        assertNotNull(commands.getSetDataNodeCommand(hostname));
    }

    @Test
    public void testGetExcludeDataNodeCommand()
    {
        String ip = "test";
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh dfs.exclude clear " + ip, commands
                .getExcludeDataNodeCommand(ip));
        assertNotNull(commands.getExcludeDataNodeCommand(ip));
    }

    @Test
    public void testGetSetTaskTrackerCommand()
    {
        String hostname = "test";
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh slaves " + hostname, commands
                .getSetTaskTrackerCommand(hostname));
        assertNotNull(commands.getSetTaskTrackerCommand(hostname));
    }

    @Test
    public void testGetExcludeTaskTrackerCommand()
    {
        String ip = "test";
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh mapred.exclude clear " + ip, commands
                .getExcludeTaskTrackerCommand(ip));
        assertNotNull(commands.getExcludeTaskTrackerCommand(ip));
    }

    @Test
    public void testGetRemoveTaskTrackerCommand()
    {
        String hostname = "test";
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh slaves clear " + hostname, commands
                .getRemoveTaskTrackerCommand(hostname));
        assertNotNull(commands.getRemoveTaskTrackerCommand(hostname));
    }

    @Test
    public void testGetIncludeTaskTrackerCommand()
    {
        String ip = "test";
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh mapred.exclude " + ip, commands
                .getIncludeTaskTrackerCommand(ip));
        assertNotNull(commands.getIncludeTaskTrackerCommand(ip));
    }

    @Test
    public void testGetRemoveDataNodeCommand()
    {
        String hostname = "test";
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh slaves clear " + hostname, commands
                .getRemoveDataNodeCommand(hostname));
        assertNotNull(commands.getRemoveDataNodeCommand(hostname));
    }

    @Test
    public void testGetIncludeDataNodeCommand()
    {
        String ip = "test";
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh mapred.exclude " + ip, commands
                .getIncludeDataNodeCommand(ip));
        assertNotNull(commands.getIncludeDataNodeCommand(ip));
    }

    @Test
    public void testGetConfigureJobTrackerCommand()
    {
        String hostname = "test";
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh slaves " + hostname, commands
                .getConfigureJobTrackerCommand(hostname));
        assertNotNull(commands.getConfigureJobTrackerCommand(hostname));
    }

    @Test
    public void testGetConfigureSecondaryNameNodeCommand()
    {
        String hostname = "test";
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh masters " + hostname, commands
                .getConfigureSecondaryNameNodeCommand(hostname));
        assertNotNull(commands.getConfigureSecondaryNameNodeCommand(hostname));
    }

    @Test
    public void testGetConfigureDataNodesCommand()
    {
        String hostname = "test";
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh slaves " + hostname, commands
                .getConfigureDataNodesCommand(hostname));
        assertNotNull(commands.getConfigureDataNodesCommand(hostname));
    }

    @Test
    public void testGetConfigureTaskTrackersCommand()
    {
        String hostname = "test";
        assertEquals(". /etc/profile && " + "hadoop-master-slave.sh slaves " + hostname, commands
                .getConfigureTaskTrackersCommand(hostname));
        assertNotNull(commands.getConfigureTaskTrackersCommand(hostname));
    }

    @Test
    public void testGetSetMastersCommand()
    {
        String namenode = "test";
        String jobtracker = "test2";
        assertEquals(". /etc/profile && " + "hadoop-configure.sh " +
                namenode + ":" + HadoopClusterConfig.NAME_NODE_PORT + " " +
                jobtracker + ":" + HadoopClusterConfig.JOB_TRACKER_PORT + " " +
                hadoopClusterConfig.getReplicationFactor(), commands.getSetMastersCommand(namenode, jobtracker));
        assertNotNull(commands.getSetMastersCommand(namenode, jobtracker));

    }
}