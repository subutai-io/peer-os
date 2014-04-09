package org.safehaus.kiskis.mgmt.impl.hadoop;

import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.hadoop.Hadoop;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.networkmanager.NetworkManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.impl.hadoop.operation.Deletion;
import org.safehaus.kiskis.mgmt.impl.hadoop.operation.Installation;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by daralbaev on 02.04.14.
 */
public class HadoopImpl implements Hadoop {
    public static final String MODULE_NAME = "Hadoop";
    private TaskRunner taskRunner;
    private DbManager dbManager;
    private Tracker tracker;
    private LxcManager lxcManager;
    private NetworkManager networkManager;
    private ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setLxcManager(LxcManager lxcManager) {
        this.lxcManager = lxcManager;
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public DbManager getDbManager() {
        return dbManager;
    }

    public Tracker getTracker() {
        return tracker;
    }

    public LxcManager getLxcManager() {
        return lxcManager;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public UUID installCluster(final Config config) {
        return new Installation(this, config).execute();
    }

    @Override
    public UUID uninstallCluster(final String clusterName) {
        return new Deletion(this).execute(clusterName);
    }


    @Override
    public List<Config> getClusters() {
        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
    }
}
