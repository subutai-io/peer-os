package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.hbase.Config;
import org.safehaus.kiskis.mgmt.api.hbase.HBase;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command(scope = "hbase", name = "list-clusters", description = "mydescription")
public class ListHBaseClustersCommand extends OsgiCommandSupport {

    private HBase hbaseManager;

    public HBase getHbaseManager() {
        return hbaseManager;
    }

    public void setHbaseManager(HBase hbaseManager) {
        this.hbaseManager = hbaseManager;
    }

    protected Object doExecute() {

        List<Config> configs = hbaseManager.getClusters();
        StringBuilder sb = new StringBuilder();

        for (Config config : configs) {
            sb.append(config.getClusterName()).append("\n");
        }

        System.out.println(sb.toString());

        return null;
    }
}
