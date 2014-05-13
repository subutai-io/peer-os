package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.hbase.HBase;

import java.util.List;

/**
 * Created by bahadyr on 5/8/14.
 */
@Command(scope = "hbase", name = "list-hadoop-clusters", description = "Shows the list of installed Hadoop clusters")
public class ListHadoopClustersCommand extends OsgiCommandSupport {

    private HBase hbaseManager;

    public HBase getHbaseManager() {
        return hbaseManager;
    }

    public void setHbaseManager(HBase hbaseManager) {
        this.hbaseManager = hbaseManager;
    }

    @Override
    protected Object doExecute() throws Exception {
        List<Config> hadoopClusters = hbaseManager.getHadoopClusters();

        if (hadoopClusters.size() > 0) {
            StringBuilder sb = new StringBuilder();

            for (Config config : hadoopClusters) {
                sb.append(config.getClusterName()).append("\n");
            }
            System.out.println(sb.toString());
        } else System.out.println("No clusters found...");

        return null;
    }
}
