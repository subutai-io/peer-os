/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.ui;


import com.vaadin.ui.Component;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import javax.naming.NamingException;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


/**
 * @author dilshat
 */
public class MongoUI implements PortalModule {
    protected static final Logger LOG = Logger.getLogger( MongoUI.class.getName() );

    public static final String MODULE_IMAGE = "mongodb.png";

    private ExecutorService executor;
    private final ServiceLocator serviceLocator;


    public MongoUI() {
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
        return MongoClusterConfig.PRODUCT_KEY;
    }


    public String getName() {
        return MongoClusterConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile( MongoUI.MODULE_IMAGE, this );
    }


    public Component createComponent() {
        try {
            return new MongoForm( executor, serviceLocator );
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
