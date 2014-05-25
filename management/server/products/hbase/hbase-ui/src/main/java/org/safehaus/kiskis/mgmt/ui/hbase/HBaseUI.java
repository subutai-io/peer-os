/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.hbase;


import com.vaadin.ui.Component;
import org.safehaus.subutai.api.agentmanager.AgentManager;
//import org.safehaus.kiskis.mgmt.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.hbase.HBaseConfig;
import org.safehaus.kiskis.mgmt.api.hbase.HBase;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author dilshat
 */
public class HBaseUI implements Module
{

    private static HBase hbaseManager;
    private static AgentManager agentManager;
    private static Tracker tracker;
    private static ExecutorService executor;
    //    private static Hadoop hadoopManager;


    public static Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        HBaseUI.tracker = tracker;
    }


    public static HBase getHbaseManager()
    {
        return hbaseManager;
    }


    public void setHbaseManager( HBase hbaseManager )
    {
        HBaseUI.hbaseManager = hbaseManager;
    }


    public static ExecutorService getExecutor()
    {
        return executor;
    }


    public static AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( AgentManager agentManager )
    {
        HBaseUI.agentManager = agentManager;
    }

    //    public static Hadoop getHadoopManager() {
    //        return hadoopManager;
    //    }
    //
    //    public void setHadoopManager(Hadoop hadoopManager) {
    //        HBaseUI.hadoopManager = hadoopManager;
    //    }


    public void init()
    {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        hbaseManager = null;
        agentManager = null;
        tracker = null;
        //        hadoopManager = null;
        executor.shutdown();
    }


    public String getName()
    {
        return HBaseConfig.PRODUCT_KEY;
    }


    public Component createComponent()
    {
        return new HBaseForm();
    }

}
