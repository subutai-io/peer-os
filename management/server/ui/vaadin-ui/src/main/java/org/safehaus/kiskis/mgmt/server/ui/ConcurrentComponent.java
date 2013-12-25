/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author dilshat
 */
public abstract class ConcurrentComponent extends VerticalLayout {

    @Override
    public synchronized void setParent(Component parent) {
        super.setParent(parent);
    }

    @Override
    public synchronized Component getParent() {
        return super.getParent();
    }

    protected void executeUpdate(Runnable update) {
        Application application = null;
        synchronized (this) {
            application = getApplication();
            if (application == null) {
                update.run();
            }
        }
        if (application != null) {
            synchronized (application) {
                update.run();
            }
        }
    }
}
