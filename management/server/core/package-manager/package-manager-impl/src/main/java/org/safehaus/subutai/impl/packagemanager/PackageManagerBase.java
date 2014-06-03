package org.safehaus.subutai.impl.packagemanager;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.packagemanager.PackageManager;
import org.safehaus.subutai.api.packagemanager.storage.PackageInfoStorage;

public abstract class PackageManagerBase implements PackageManager {

    AgentManager agentManager;
    CommandRunner commandRunner;
    DbManager dbManager;

    PackageInfoStorage storage;

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
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

    public PackageInfoStorage getStorage() {
        return storage;
    }

    public void setStorage(PackageInfoStorage storage) {
        this.storage = storage;
    }

}
