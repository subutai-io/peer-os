package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.shark.Config;
import org.safehaus.kiskis.mgmt.api.shark.Shark;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;

import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command(scope = "shark", name = "uninstall-cluster", description = "Command to uninstall Shark cluster")
public class UninstallClusterCommand extends OsgiCommandSupport {

    private Shark sharkManager;
    private Tracker tracker;

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setSharkManager(Shark sharkManager) {
        this.sharkManager = sharkManager;
    }

    public Shark getSharkManager() {
        return sharkManager;
    }


    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
    String clusterName = null;

    protected Object doExecute() {
        UUID uuid = sharkManager.uninstallCluster(clusterName);
        int logSize = 0;
        while (!Thread.interrupted()) {
            ProductOperationView po = tracker.getProductOperation(Config.PRODUCT_KEY, uuid);
            if (po != null) {
                if (logSize != po.getLog().length()) {
                    System.out.print(po.getLog().substring(logSize, po.getLog().length()));
                    System.out.flush();
                    logSize = po.getLog().length();
                }
                if (po.getState() != ProductOperationState.RUNNING) {
                    break;
                }
            } else {
                System.out.println("Product operation not found. Check logs");
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
        }
        return null;
    }
}
