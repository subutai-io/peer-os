package org.safehaus.subutai.plugin.storm.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class StormUI implements PortalModule {
    protected static final Logger LOG = Logger.getLogger( StormUI.class.getName() );

    public static final String MODULE_IMAGE = "storm.png";


    private ExecutorService executor;
    private final ServiceLocator serviceLocator;


    public StormUI() {
        serviceLocator = new ServiceLocator();
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {

        executor.shutdown();
    }


    @Override
    public String getId() {
        return StormConfig.PRODUCT_NAME;
    }


    @Override
    public String getName() {
        return StormConfig.PRODUCT_NAME;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile( StormUI.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent() {
        try {
            return new StormForm( executor, serviceLocator );
        }
        catch ( NamingException e ) {
            LOG.severe( e.getMessage() );
        }

        return null;
    }
}
