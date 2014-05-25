package org.safehaus.kiskis.mgmt.ui.tracker;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

import com.vaadin.ui.Component;


public class TrackerUI implements Module {

    public static final String MODULE_NAME = "Tracker";
    private static Tracker tracker;
    private static ExecutorService executor;


    public static Tracker getTracker() {
        return tracker;
    }


    public void setTracker( Tracker tracker ) {
        TrackerUI.tracker = tracker;
    }


    public static ExecutorService getExecutor() {
        return executor;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        tracker = null;
        executor.shutdown();
    }


    @Override
    public String getName() {
        return TrackerUI.MODULE_NAME;
    }


    @Override
    public Component createComponent() {
        TrackerForm trackerForm = new TrackerForm();
        trackerForm.refreshSources();
        return trackerForm;
    }
}
