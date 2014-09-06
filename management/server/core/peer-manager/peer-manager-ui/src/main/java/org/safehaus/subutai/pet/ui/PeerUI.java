package org.safehaus.subutai.pet.ui;


import java.io.File;

import org.safehaus.subutai.pet.api.PeerManager;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.common.util.FileUtil;

import com.vaadin.ui.Component;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PeerUI implements PortalModule {

    public static final String MODULE_IMAGE = "peer.png";
    public static final String MODULE_NAME = "Peer";
    private PeerManager petManager;


    public PeerManager getPetManager() {
        return petManager;
    }


    public void setPetManager( final PeerManager petManager ) {
        this.petManager = petManager;
    }


    public void init() {}


    public void destroy() {}


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
        return FileUtil.getFile( MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent() {
        return new PeerForm();
    }
}
