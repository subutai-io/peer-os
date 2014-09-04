/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.oozie.ui;


import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.plugin.oozie.api.OozieConfig;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


/**
 * @author dilshat
 */
public class OozieUI extends OozieBase implements PortalModule {

    public static final String MODULE_IMAGE = "oozie.png";


    public OozieUI() {}


    public ExecutorService getExecutor() {
        return executor;
    }


    public void init() {
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        oozieManager = null;
        agentManager = null;
        tracker = null;
        hadoopManager = null;
        executor.shutdown();
    }


    @Override
    public String getId() {
        return OozieConfig.PRODUCT_KEY;
    }


    public String getName() {
        return OozieConfig.PRODUCT_KEY;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile( OozieUI.MODULE_IMAGE, this );
    }


    public Component createComponent() {
        return new OozieForm( this );
    }
}
