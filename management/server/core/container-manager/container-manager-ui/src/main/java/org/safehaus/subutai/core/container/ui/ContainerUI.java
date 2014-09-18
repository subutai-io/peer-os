package org.safehaus.subutai.core.container.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class ContainerUI implements PortalModule {

    protected static final Logger LOG = Logger.getLogger( ContainerUI.class.getName() );

    public static final String MODULE_IMAGE = "lxc.png";
    public static final String MODULE_NAME = "Container";
    private ExecutorService executor;
    private final ServiceLocator serviceLocator;


    public ContainerUI()  {
        this.serviceLocator = new ServiceLocator();
    }


    public void init() {
        executor = Executors.newFixedThreadPool( 5 );
    }


    public void destroy() {
        executor.shutdown();
    }


    @Override
    public String getId() {
        return MODULE_NAME;
    }


    @Override
    public String getName() {
        return MODULE_NAME;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile( ContainerUI.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent() {
        try {
            return new ContainerForm( executor, serviceLocator );
        }
        catch ( NamingException e ) {
            LOG.severe( e.getMessage() );
        }

        return null;
    }
}
