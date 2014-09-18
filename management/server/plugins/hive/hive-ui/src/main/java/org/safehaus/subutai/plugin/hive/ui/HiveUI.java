package org.safehaus.subutai.plugin.hive.ui;


import com.vaadin.ui.Component;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import javax.naming.NamingException;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


public class HiveUI implements PortalModule {

    protected static final Logger LOG = Logger.getLogger( HiveUI.class.getName() );
    public static final String MODULE_IMAGE = "hive.png";

    private ExecutorService executor;
    private final ServiceLocator serviceLocator;


    public HiveUI()  {
        this.serviceLocator = new ServiceLocator();
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {

        executor.shutdown();
    }


    @Override
    public String getId() {
        return HiveConfig.PRODUCT_KEY;
    }


    @Override
    public String getName() {
        return HiveConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile( HiveUI.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent() {
        try {
            return new HiveForm( executor, serviceLocator );
        }
        catch ( NamingException e ) {
            LOG.severe( e.getMessage() );
        }

        return null;
    }

    @Override
    public Boolean isCorePlugin() {
        return false;
    }
}
