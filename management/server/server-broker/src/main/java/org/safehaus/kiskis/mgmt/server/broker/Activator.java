/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.broker;

import org.apache.activemq.broker.BrokerService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.CommandSendInterface;

/**
 *
 * @author bahadyr
 */
public class Activator implements BundleActivator {

    private BrokerService broker = new BrokerService();
    private static ServiceReference refCommandSender;
    private static BundleContext context;

    public void start(BundleContext context) throws Exception {
        try {
            Activator.context = context;
            Activator.refCommandSender =
                    context.getServiceReference(CommandSendInterface.class.getName());

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public void stop(BundleContext bc) throws Exception {
        try {
            broker.stop();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
    
    public static CommandSendInterface getServerBroker() {
        return ((CommandSendInterface) Activator.context.getService(Activator.refCommandSender));
    }
}
