package org.safehaus.subutai.core.backup.ui;


import java.io.File;

import com.vaadin.ui.Component;


/**
 * This interface describes a module which can be added to portal
 *
 * @author Guillaume Lamirand
 */
public interface PortalModule
{

    /**
     * Return a technical id which has to be unique in module register
     *
     * @return technical id
     */
    String getId();

    /**
     * Return a name used to define this module
     *
     * @return name
     */
    String getName();

    /**
     * Return an image associated with this module
     *
     * @return name
     */
    File getImage();

    /**
     * Return a new {@link com.vaadin.ui.Component} used to displayed this module
     *
     * @return a new {@link com.vaadin.ui.Component}
     */
    Component createComponent();

    /**
     * Function to differentiate core plugins from plugins needed to show-up in different tabs in main dashboard
     */
    Boolean isCorePlugin();
}
