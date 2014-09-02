package org.safehaus.subutai.plugin.solr.cli;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.api.Solr;
import org.safehaus.subutai.api.tracker.Tracker;

import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command(scope = "solr", name = "uninstall-cluster", description = "Command to uninstall Solr cluster")
public class UninstallClusterCommand extends OsgiCommandSupport {

    private Solr solrManager;
    private Tracker tracker;

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setSolrManager(Solr solrManager) {
        this.solrManager = solrManager;
    }

    public Solr getSolrManager() {
        return solrManager;
    }


    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
    String clusterName = null;

    protected Object doExecute() {
        UUID uuid = solrManager.uninstallCluster(clusterName);
        int logSize = 0;
        while (!Thread.interrupted()) {
            ProductOperationView po = tracker.getProductOperation( SolrClusterConfig.PRODUCT_KEY, uuid);
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
