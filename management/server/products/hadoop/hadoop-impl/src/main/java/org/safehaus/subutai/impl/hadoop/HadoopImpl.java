package org.safehaus.subutai.impl.hadoop;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.hadoop.HadoopClusterConfig;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.impl.hadoop.operation.Adding;
import org.safehaus.subutai.impl.hadoop.operation.Deletion;
import org.safehaus.subutai.impl.hadoop.operation.Installation;
import org.safehaus.subutai.impl.hadoop.operation.configuration.DataNode;
import org.safehaus.subutai.impl.hadoop.operation.configuration.JobTracker;
import org.safehaus.subutai.impl.hadoop.operation.configuration.NameNode;
import org.safehaus.subutai.impl.hadoop.operation.configuration.SecondaryNameNode;
import org.safehaus.subutai.impl.hadoop.operation.configuration.TaskTracker;


/**
 * Created by daralbaev on 02.04.14.
 */
public class HadoopImpl implements Hadoop {
    public static final String MODULE_NAME = "Hadoop";
    private static CommandRunner commandRunner;
    private static Tracker tracker;
    private AgentManager agentManager;
    private DbManager dbManager;
    private LxcManager lxcManager;
    private NetworkManager networkManager;
    private ExecutorService executor;


    public static CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public void setCommandRunner( CommandRunner commandRunner ) {
        HadoopImpl.commandRunner = commandRunner;
    }


    public static Tracker getTracker() {
        return tracker;
    }


    public void setTracker( Tracker tracker ) {
        this.tracker = tracker;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
        commandRunner = null;
    }


    public DbManager getDbManager() {
        return dbManager;
    }


    public void setDbManager( DbManager dbManager ) {
        this.dbManager = dbManager;
    }


    public LxcManager getLxcManager() {
        return lxcManager;
    }


    public void setLxcManager( LxcManager lxcManager ) {
        this.lxcManager = lxcManager;
    }


    public NetworkManager getNetworkManager() {
        return networkManager;
    }


    public void setNetworkManager( NetworkManager networkManager ) {
        this.networkManager = networkManager;
    }


    public ExecutorService getExecutor() {
        return executor;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    @Override
    public UUID installCluster( final HadoopClusterConfig hadoopClusterConfig ) {
        return new Installation( this, hadoopClusterConfig ).execute();
    }


    @Override
    public UUID uninstallCluster( final String clusterName ) {
        return new Deletion( this ).execute( clusterName );
    }


    @Override
    public List<HadoopClusterConfig> getClusters() {
        return dbManager.getInfo( HadoopClusterConfig.PRODUCT_KEY, HadoopClusterConfig.class );
    }


    @Override
    public HadoopClusterConfig getCluster( String clusterName ) {
        return dbManager.getInfo( HadoopClusterConfig.PRODUCT_KEY, clusterName, HadoopClusterConfig.class );
    }


    @Override
    public UUID startNameNode( HadoopClusterConfig hadoopClusterConfig ) {
        return new NameNode( this, hadoopClusterConfig ).start();
    }


    @Override
    public UUID stopNameNode( HadoopClusterConfig hadoopClusterConfig ) {
        return new NameNode( this, hadoopClusterConfig ).stop();
    }


    @Override
    public UUID restartNameNode( HadoopClusterConfig hadoopClusterConfig ) {
        return new NameNode( this, hadoopClusterConfig ).restart();
    }


    @Override
    public UUID statusNameNode( HadoopClusterConfig hadoopClusterConfig ) {
        return new NameNode( this, hadoopClusterConfig ).status();
    }


    @Override
    public UUID statusSecondaryNameNode( HadoopClusterConfig hadoopClusterConfig ) {
        return new SecondaryNameNode( this, hadoopClusterConfig ).status();
    }


    @Override
    public UUID statusDataNode( Agent agent ) {
        return new DataNode( this, null ).status( agent );
    }


    @Override
    public UUID startJobTracker( HadoopClusterConfig hadoopClusterConfig ) {
        return new JobTracker( this, hadoopClusterConfig ).start();
    }


    @Override
    public UUID stopJobTracker( HadoopClusterConfig hadoopClusterConfig ) {
        return new JobTracker( this, hadoopClusterConfig ).stop();
    }


    @Override
    public UUID restartJobTracker( HadoopClusterConfig hadoopClusterConfig ) {
        return new JobTracker( this, hadoopClusterConfig ).restart();
    }


    @Override
    public UUID statusJobTracker( HadoopClusterConfig hadoopClusterConfig ) {
        return new JobTracker( this, hadoopClusterConfig ).status();
    }


    @Override
    public UUID statusTaskTracker( Agent agent ) {
        return new TaskTracker( this, null ).status( agent );
    }


    @Override
    public UUID addNode( String clusterName ) {
        return new Adding( this, clusterName ).execute();
    }


    @Override
    public UUID blockDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {
        return new DataNode( this, hadoopClusterConfig ).block( agent );
    }


    @Override
    public UUID blockTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {
        return new TaskTracker( this, hadoopClusterConfig ).block( agent );
    }


    @Override
    public UUID unblockDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {
        return new DataNode( this, hadoopClusterConfig ).unblock( agent );
    }


    @Override
    public UUID unblockTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent ) {
        return new TaskTracker( this, hadoopClusterConfig ).unblock( agent );
    }
}
