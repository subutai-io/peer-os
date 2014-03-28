package org.safehaus.kiskis.mgmt.ui.tracker;

import com.vaadin.ui.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class TrackerUI implements Module {

    public static final String MODULE_NAME = "Tracker";
    private static DbManager dbManager;
    private static ExecutorService executor;

    public static DbManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DbManager dbManager) {
        TrackerUI.dbManager = dbManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        dbManager = null;
        executor.shutdown();
    }

    @Override
    public String getName() {
        return TrackerUI.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new TrackerForm();
    }

}
