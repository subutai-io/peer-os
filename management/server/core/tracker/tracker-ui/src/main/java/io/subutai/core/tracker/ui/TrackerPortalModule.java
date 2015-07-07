package io.subutai.core.tracker.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.subutai.common.util.FileUtil;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.server.ui.api.PortalModule;

import com.google.common.base.Preconditions;
import com.vaadin.ui.Component;


public class TrackerPortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "tracker.png";
    public static final String MODULE_NAME = "Tracker";
    private final Tracker tracker;
    private ExecutorService executor;


    public TrackerPortalModule( final Tracker tracker )
    {
        Preconditions.checkNotNull( tracker, "Tracker is null" );

        this.tracker = tracker;
    }


    public void init()
    {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    @Override
    public String getId()
    {
        return TrackerPortalModule.MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return TrackerPortalModule.MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( TrackerPortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        final TrackerComponent trackerComponent = new TrackerComponent( tracker, executor );
        trackerComponent.refreshSources();
        trackerComponent.startTracking();
        return trackerComponent;
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
