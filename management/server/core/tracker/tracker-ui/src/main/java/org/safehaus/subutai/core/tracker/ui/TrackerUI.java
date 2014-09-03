package org.safehaus.subutai.core.tracker.ui;


import com.vaadin.ui.Component;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.server.ui.api.PortalModule;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TrackerUI implements PortalModule
{

    public static final String MODULE_IMAGE = "tracker.png";
    public static final String MODULE_NAME = "Tracker";
    private static TrackerForm trackerForm;
    private static Tracker tracker;
    private static ExecutorService executor;


    public static Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        TrackerUI.tracker = tracker;
    }


    public static ExecutorService getExecutor()
    {
        return executor;
    }


    public void init()
    {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        tracker = null;
        executor.shutdown();
    }


    @Override
    public String getId()
    {
        return TrackerUI.MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return TrackerUI.MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( TrackerUI.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        trackerForm = new TrackerForm();
        trackerForm.refreshSources();
        trackerForm.startTracking();
        return trackerForm;
    }
}
