package org.safehaus.subutai.pet.ui;


import java.io.File;

import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.shared.protocol.FileUtil;

import com.vaadin.ui.Component;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PetUI implements PortalModule {

    public static final String MODULE_IMAGE = "env.png";
    public static final String MODULE_NAME = "Pet";


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
        return FileUtil.getFile(MODULE_IMAGE, this);
    }


    @Override
    public Component createComponent() {
        return new PetForm();
    }
}
