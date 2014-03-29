/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.mongodb;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.api.mongodb.Mongo;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

/**
 *
 * @author dilshat
 */
public class MongoUI implements Module {

    public static final String MODULE_NAME = "Mongo";
    private static Mongo mongoManager;
    private static TaskRunner taskRunner;
    private static AgentManager agentManager;
    private static ExecutorService executor;

    public static Mongo getMongoManager() {
        return mongoManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void setMongoManager(Mongo mongoManager) {
        MongoUI.mongoManager = mongoManager;
    }

    public static TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        MongoUI.taskRunner = taskRunner;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        MongoUI.agentManager = agentManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        mongoManager = null;
        taskRunner = null;
        agentManager = null;
        executor.shutdown();
    }

    public static void showProgressWindow(UUID trackID, final Window.CloseListener closeCallback) {
        Window progressWindow = MgmtApplication.createProgressWindow(Config.PRODUCT_KEY, trackID);
        MgmtApplication.addCustomWindow(progressWindow);
        if (closeCallback != null) {
            progressWindow.addListener(new Window.CloseListener() {

                @Override
                public void windowClose(Window.CloseEvent e) {
                    closeCallback.windowClose(e);
                }
            });
        }
    }

    public String getName() {
        return MODULE_NAME;
    }

    public Component createComponent() {
        return new MongoForm();
    }

}
