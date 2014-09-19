/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.lucene.ui;


import com.vaadin.ui.Component;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.lucene.api.Config;
import org.safehaus.subutai.server.ui.api.PortalModule;

import javax.naming.NamingException;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


/**
 * @author dilshat
 */
public class LuceneUI implements PortalModule {
    protected static final Logger LOG = Logger.getLogger( LuceneUI.class.getName() );

    public static final String MODULE_IMAGE = "lucene.png";

    private ExecutorService executor;
    private final ServiceLocator serviceLocator;


    public LuceneUI()  {
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
        return Config.PRODUCT_KEY;
    }


    public String getName() {
        return Config.PRODUCT_KEY;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile( LuceneUI.MODULE_IMAGE, this );
    }


    public Component createComponent() {
        try {
            return new LuceneForm( executor, serviceLocator );
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
