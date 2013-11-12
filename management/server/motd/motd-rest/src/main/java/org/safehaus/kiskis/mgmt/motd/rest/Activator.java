package org.safehaus.kiskis.mgmt.motd.rest;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author : yigit
 */
public class Activator implements BundleActivator {

    private MotdRestService myService;

    public void start(BundleContext context) {
        System.out.println("Starting : " + context.getBundle().getSymbolicName());
        try {
            System.out.println("Message : " + myService.getMessage());
        } catch (Exception e) {
            System.out.println("Err : " + e.getMessage());
        }
    }

    public void stop(BundleContext context) {
        System.out.println("Stopping : " + context.getBundle().getSymbolicName());
    }
}
