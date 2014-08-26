package org.safehaus.subutai.cli.commands;

import java.io.IOException;
import java.util.UUID;

import org.safehaus.subutai.api.cassandra.Cassandra;
import org.safehaus.subutai.api.cassandra.CassandraConfig;
import org.safehaus.subutai.api.tracker.Tracker;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "install-cluster", description = "Command to install Cassandra cluster")
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

    @Argument(index = 1, name = "domainName", description = "The domain name of the cluster.", required = true, multiValued = false)
    String domainName = null;

    @Argument(index = 2, name = "numberOfNodes", description = "Number of nodes in cluster.", required = true, multiValued = false)
    String numberOfNodes = null;

    @Argument(index = 3, name = "numberOfSeeds", description = "Number of seeds in cluster.", required = true, multiValued = false)
    String numberOfSeeds = null;

    protected Object doExecute() throws IOException {
        CassandraConfig config = new CassandraConfig();
        config.setClusterName(clusterName);
        config.setDomainName(domainName);
        config.setNumberOfNodes(Integer.parseInt(numberOfNodes));
        config.setNumberOfSeeds(Integer.parseInt(numberOfSeeds));

        UUID uuid = cassandraManager.installCluster(config);
        tracker.printOperationLog(CassandraConfig.PRODUCT_KEY, uuid, 30000);
        return null;
    }
}
