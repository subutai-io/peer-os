/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.broker;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;

/**
 *
 * @author bahadyr
 */
public class Activator implements BundleActivator {
    private static BundleContext context;

    public void start(BundleContext context) throws Exception {
        try {
            Activator.context = context;
            
            System.out.println("Broker Activator started");
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
    
    public static CommandTransportInterface getCommandSender() {
        ServiceReference refCommandSender =
                    Activator.context.getServiceReference(CommandTransportInterface.class.getName());
        return ((CommandTransportInterface) Activator.context.getService(refCommandSender));
    }
}
