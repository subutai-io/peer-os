package io.subutai.core.registration.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.base.Preconditions;
import com.vaadin.ui.Component;

import io.subutai.common.util.FileUtil;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.server.ui.api.PortalModule;


public class NodeRegistrationPortalModule implements PortalModule
{
    public static final String MODULE_IMAGE = "logo.png";
    public static final String MODULE_NAME = "Plugin";
    private RegistrationManager registrationManager;
    private ExecutorService executor;
    private Tracker tracker;


    public NodeRegistrationPortalModule( final RegistrationManager registrationManager, final Tracker tracker )
    {
        Preconditions.checkNotNull( registrationManager );
        Preconditions.checkNotNull( tracker );

        this.registrationManager = registrationManager;
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
        return MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {

        return new NodeRegistrationComponent( executor, this, registrationManager, tracker );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
