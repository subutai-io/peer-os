package org.safehaus.kiskis.mgmt.impl.hive.query;

import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.hive.query.HiveQuery;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class HiveQueryBase implements HiveQuery {

    protected CommandRunner commandRunner;
    protected DbManager dbManager;

    protected ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public void setCommandRunner(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public DbManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

}
