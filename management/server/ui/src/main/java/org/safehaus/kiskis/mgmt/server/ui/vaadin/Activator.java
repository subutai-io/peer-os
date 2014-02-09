/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.vaadin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.broker.impl.ResponseStorage;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.CommandSendInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.RegisteredHostInterface;

/**
 *
 * @author bahadyr
 */
public class Activator implements BundleActivator {
    private static BundleContext context;

    public void start(BundleContext context) throws Exception {
        try {
            Activator.context = context;
            
            System.out.println("UI Activator started");
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public void stop(BundleContext bc) throws Exception {
        try {
            
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public static RegisteredHostInterface getServerBroker() {
        ServiceReference refServerBroker =
                Activator.context.getServiceReference(RegisteredHostInterface.class.getName());
        return ((RegisteredHostInterface) Activator.context.getService(refServerBroker));
    }
}
