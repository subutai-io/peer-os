package org.safehaus.subutai.core.shiro.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class ShiroManagerPortalModule implements PortalModule
{

    private final static String MODULE_IMAGE = "shiro.png";
    private final static String MODULE_NAME = "Shiro Manager";
    private ExecutorService executor;


    public ExecutorService getExecutor()
    {
        return executor;
    }


    public void setExecutor( final ExecutorService executor )
    {
        this.executor = executor;
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
        return new ShiroManagerComponent( this );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}