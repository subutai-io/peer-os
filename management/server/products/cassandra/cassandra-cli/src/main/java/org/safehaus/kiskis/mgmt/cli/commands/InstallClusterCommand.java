package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.cassandra.Cassandra;
import org.safehaus.kiskis.mgmt.api.cassandra.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationState;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperationView;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;

import java.io.IOException;
import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "uninstall-cluster", description = "Command to uninstall Cassandra cluster")
public class InstallClusterCommand extends OsgiCommandSupport {

    private static Cassandra cassandraManager;
    private static Tracker tracker;

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        InstallClusterCommand.tracker = tracker;
    }

    public void setCassandraManager(Cassandra cassandraManager) {
        InstallClusterCommand.cassandraManager = cassandraManager;
    }

    public static Cassandra getCassandraManager() {
        return cassandraManager;
    }


    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
    String clusterName = null;

    @Argument(index = 1, name = "domainName", description = "The name of the cluster.", required = true, multiValued = false)
    String domainName = null;

    @Argument(index = 2, name = "numberOfNodes", description = "The name of the cluster.", required = true, multiValued = false)
    String numberOfNodes = null;

    @Argument(index = 3, name = "numberOfSeeds", description = "The name of the cluster.", required = true, multiValued = false)
    String numberOfSeeds = null;

    protected Object doExecute() throws IOException {
        Config config = new Config();
        config.setClusterName(clusterName);
        config.setDomainName(domainName);
        config.setNumberOfNodes(Integer.parseInt(numberOfNodes));
        config.setNumberOfSeeds(Integer.parseInt(numberOfSeeds));

        UUID uuid = cassandraManager.installCluster(config);
//        ProductOperationView pow = tracker.getProductOperation(Config.PRODUCT_KEY, uuid);
//        Runtime.getRuntime().exec("cls");
//        System.out.println(pow.getLog());
//        int logSize = 0;
        while (!Thread.interrupted()) {
            ProductOperationView po = tracker.getProductOperation(Config.PRODUCT_KEY, uuid);
            if (po != null) {
//                if( logSize !=  po.getLog().length()) {
//                    System.out.print(po.getLog().substring(logSize, po.getLog().length()));
                System.out.println(po.getLog());
//                    logSize = po.getLog().length();
//                }
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

        System.out.println(String.format("Cassandra cluster %s installed.", clusterName));
        return null;
    }
}
