package org.safehaus.subutai.plugin.flume.cli;

import java.util.UUID;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.plugin.flume.api.Flume;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;
import org.safehaus.subutai.api.tracker.Tracker;

/**
 * Displays the last log entries
 */
@Command(scope = "flume", name = "uninstall-cluster", description = "Command to uninstall Flume cluster")
public class UninstallClusterCommand extends OsgiCommandSupport {

    private Flume flumeManager;
    private Tracker tracker;

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setFlumeManager(Flume flumeManager) {
        this.flumeManager = flumeManager;
    }

    public Flume getFlumeManager() {
        return flumeManager;
    }

    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
    String clusterName = null;

    @Override
    protected Object doExecute() {
        UUID uuid = flumeManager.uninstallCluster(clusterName);
        int logSize = 0;
        while(!Thread.interrupted()) {
            ProductOperationView po = tracker.getProductOperation(FlumeConfig.PRODUCT_KEY, uuid);
            if(po != null) {
                if(logSize != po.getLog().length()) {
                    System.out.print(po.getLog().substring(logSize, po.getLog().length()));
                    System.out.flush();
                    logSize = po.getLog().length();
                }
                if(po.getState() != ProductOperationState.RUNNING)
                    break;
            } else {
                System.out.println("Product operation not found. Check logs");
                break;
            }
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                break;
            }
        }
        return null;
    }
}
