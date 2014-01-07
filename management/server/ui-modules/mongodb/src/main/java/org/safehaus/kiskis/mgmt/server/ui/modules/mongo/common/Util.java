/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Label;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class Util {

    private static final Logger LOG = Logger.getLogger(Util.class.getName());

    public static Label createImage(String imageName, int imageWidth, int imageHeight) {
        Label image = new Label(
                String.format("<img src='http://%s:%s/%s' />", MgmtApplication.APP_URL, Common.WEB_SERVER_PORT, imageName));
        image.setContentMode(Label.CONTENT_XHTML);
        image.setHeight(imageWidth, Sizeable.UNITS_PIXELS);
        image.setWidth(imageHeight, Sizeable.UNITS_PIXELS);
        return image;
    }

}
