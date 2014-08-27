package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.cassandra.Cassandra;
import org.safehaus.subutai.api.cassandra.Config;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "describe-cluster", description = "Shows the details of the Cassandra cluster.")
public class DescribeClusterCommand extends OsgiCommandSupport {

    private static Cassandra cassandraManager;

    public void setCassandraManager(Cassandra cassandraManager) {
        DescribeClusterCommand.cassandraManager = cassandraManager;
    }

    public static Cassandra getCassandraManager() {
        return cassandraManager;
    }

    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
    String clusterName = null;

    protected Object doExecute() {
        Config config = cassandraManager.getCluster(clusterName);
        if (config != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cluster name: ").append(config.getClusterName()).append("\n");
            sb.append("Nodes:").append("\n");
            for (Agent agent : config.getNodes()) {
                sb.append("Hostname: ").append(agent.getHostname())
                        .append(", Agent UUID: ").append(agent.getUuid())
                        .append("\n");
            }
            sb.append("Seeds:").append("\n");
            for (Agent agent : config.getSeedNodes()) {
                sb.append("Hostname: ").append(agent.getHostname())
                        .append(", Agent UUID: ").append(agent.getUuid())
                        .append("\n");
            }
            sb.append("Data directory: ").append(config.getDataDirectory()).append("\n");
            sb.append("Commit log directory: ").append(config.getCommitLogDirectory()).append("\n");
            sb.append("Saved cache directory: ").append(config.getSavedCachesDirectory()).append("\n");
            sb.append("Domain name: ").append(config.getDomainName()).append("\n");
            System.out.println(sb.toString());
        } else System.out.println("No clusters found...");

        return null;
    }
}
