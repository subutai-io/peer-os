/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


/**
 * @author dilshat
 */
public class HBaseUI implements PortalModule
{

    public static final String MODULE_IMAGE = "hbase.png";

    private HBase hbaseManager;
    private AgentManager agentManager;
    private Hadoop hadoopManager;
    private Tracker tracker;
    private CommandRunner commandRunner;
    private ExecutorService executor;


    public HBaseUI( AgentManager agentManager, Tracker tracker, HBase hbaseManager, CommandRunner commandRunner,
                    Hadoop hadoopManager )
    {
        this.agentManager = agentManager;
        this.tracker = tracker;
        this.hbaseManager = hbaseManager;
        this.commandRunner = commandRunner;
        this.hadoopManager = hadoopManager;
    }


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( final Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public HBase getHbaseManager()
    {
        return hbaseManager;
    }


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public void init()
    {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        hbaseManager = null;
        hadoopManager = null;
        agentManager = null;
        tracker = null;
        commandRunner = null;
        executor.shutdown();
    }


    @Override
    public String getId()
    {
        return HBaseClusterConfig.PRODUCT_KEY;
    }


    public String getName()
    {
        return HBaseClusterConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( HBaseUI.MODULE_IMAGE, this );
    }


    public Component createComponent()
    {
        return new HBaseForm( this );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return false;
    }
}
