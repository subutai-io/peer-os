/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.cassandra.ui;


import com.vaadin.ui.Component;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CassandraUI implements PortalModule {

    public final String MODULE_IMAGE = "cassandra.png";
    public final String PRODUCT_KEY = "Cassandra";
    public final String PRODUCT_NAME = "Cassandra";

    private Cassandra cassandraManager;
    private AgentManager agentManager;
    private CommandRunner commandRunner;
    private Tracker tracker;
    private ExecutorService executor;


    public CassandraUI( AgentManager agentManager, Cassandra cassandraManager, Tracker tracker,
                        CommandRunner commandRunner ) {
        this.cassandraManager = cassandraManager;
        this.agentManager = agentManager;
        this.tracker = tracker;
        this.commandRunner = commandRunner;
    }


    public Tracker getTracker() {
        return tracker;
    }


    public Cassandra getCassandraManager() {
        return cassandraManager;
    }


    public ExecutorService getExecutor() {
        return executor;
    }


    public AgentManager getAgentManager() {
        return agentManager;
    }


    public CommandRunner getCommandRunner() {
        return commandRunner;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        cassandraManager = null;
        agentManager = null;
        tracker = null;
        executor.shutdown();
    }


    @Override
    public String getId() {
        return PRODUCT_KEY;
    }


    public String getName() {
        return PRODUCT_KEY;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile( MODULE_IMAGE, this );
    }


    public Component createComponent() {
        return new CassandraForm(this);
    }

    @Override
    public Boolean isCorePlugin() {
        return false;
    }
}
