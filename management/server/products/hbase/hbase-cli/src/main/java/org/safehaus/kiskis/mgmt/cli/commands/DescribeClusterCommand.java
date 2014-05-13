package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.hbase.HBase;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "describe-cluster", description = "Shows the details of the Cassandra cluster.")
public class DescribeClusterCommand extends OsgiCommandSupport {

    private HBase hbaseManager;

    public HBase getHbaseManager() {
        return hbaseManager;
    }

    public void setHbaseManager(HBase hbaseManager) {
        this.hbaseManager = hbaseManager;
    }

    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true, multiValued = false)
    String clusterName = null;

    protected Object doExecute() {
        Config config = hbaseManager.getHadoopCluster(clusterName);
        if (config != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cluster name: ").append(config.getClusterName()).append("\n");
            sb.append("Domain name: ").append(config.getDomainName()).append("\n");
            sb.append("All nodes:").append("\n");
            for (Agent agent : config.getAllNodes()) {
                sb.append(agent.getHostname()).append("\n");
            }
            sb.append("Slave nodes:").append("\n");
            for (Agent agent : config.getAllSlaveNodes()) {
                sb.append(agent.getHostname()).append("\n");
            }
            sb.append("Data nodes:").append("\n");
            for (Agent agent : config.getDataNodes()) {
                sb.append(agent.getHostname()).append("\n");
            }
            sb.append("Task trackers:").append("\n");
            for (Agent agent : config.getTaskTrackers()) {
                sb.append(agent.getHostname()).append("\n");
            }
            sb.append("Job tracker:").append(config.getJobTracker()).append("\n");
            System.out.println(sb.toString());
        } else System.out.println("No clusters found...");

        return null;
    }
}
