package org.safehaus.subutai.plugin.spark.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class SparkUI implements PortalModule {

    public static final String MODULE_IMAGE = "spark.png";
    protected static final Logger LOG = Logger.getLogger( SparkUI.class.getName() );

    private ExecutorService executor;
    private final ServiceLocator serviceLocator;


    public SparkUI(){
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
        return SparkClusterConfig.PRODUCT_KEY;
    }


    @Override
    public String getName() {
        return SparkClusterConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile( SparkUI.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent() {
        try {
            return new SparkForm( executor, serviceLocator );
        }
        catch ( NamingException e ) {
            LOG.severe( e.getMessage() );
        }
        return null;
    }
}
