package org.safehaus.subutai.plugin.oozie.ui;


import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.oozie.api.Oozie;


/**
 * Created by bahadyr on 9/4/14.
 */
abstract class OozieBase {


    Oozie oozieManager;
    AgentManager agentManager;
    Tracker tracker;
    Hadoop hadoopManager;
    CommandRunner commandRunner;
    ExecutorService executor;


    public Oozie getOozieManager() {
        return oozieManager;
    }


    public void setOozieManager( final Oozie oozieManager ) {
        this.oozieManager = oozieManager;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public void setAgentManager( final AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public void setTracker( final Tracker tracker ) {
        this.tracker = tracker;
    }


    public Hadoop getHadoopManager() {
        return hadoopManager;
    }


    public void setHadoopManager( final Hadoop hadoopManager ) {
        this.hadoopManager = hadoopManager;
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public void setCommandRunner( final CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    public ExecutorService getExecutor() {
        return executor;
    }


    public void setExecutor( final ExecutorService executor ) {
        this.executor = executor;
    }
}
