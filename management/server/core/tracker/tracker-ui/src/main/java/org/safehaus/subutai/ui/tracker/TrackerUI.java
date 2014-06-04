package org.safehaus.subutai.ui.tracker;


import com.vaadin.ui.Component;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TrackerUI implements PortalModule {

    public static TrackerForm trackerForm;
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
    public String getId() {
        return TrackerUI.MODULE_NAME;
    }

    @Override
    public String getName() {
        return TrackerUI.MODULE_NAME;
    }


    @Override
    public Component createComponent() {
        /*if(trackerForm == null){

        }*/

        trackerForm = new TrackerForm();
        trackerForm.refreshSources();
        trackerForm.startTracking();
        return trackerForm;
    }
}
