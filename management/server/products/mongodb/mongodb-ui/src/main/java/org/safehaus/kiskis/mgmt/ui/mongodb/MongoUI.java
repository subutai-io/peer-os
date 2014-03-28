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
import org.safehaus.kiskis.mgmt.api.mongodb.Mongo;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.ui.mongodb.manager.Manager;
import org.safehaus.kiskis.mgmt.ui.mongodb.window.ProgressWindow;

/**
 *
 * @author dilshat
 */
public class MongoUI implements Module {

    public static final String MODULE_NAME = "Mongo";
    private static Mongo mongoManager;
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

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        mongoManager = null;
        executor.shutdown();
    }

    public static void showProgressWindow(final Manager manager, UUID trackID) {
        ProgressWindow progressWindow = new ProgressWindow(trackID);
        MgmtApplication.addCustomWindow(progressWindow);
        progressWindow.addListener(new Window.CloseListener() {

            @Override
            public void windowClose(Window.CloseEvent e) {
                manager.refreshClustersInfo();

            }
        });
    }

    public String getName() {
        return MODULE_NAME;
    }

    public Component createComponent() {
        return new MongoForm();
    }

}
