package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.hbase.HBase;
import org.safehaus.subutai.core.tracker.api.Tracker;


/**
 * Displays the last log entries
 */
@Command(scope = "hbase", name = "install-cluster", description = "Command to install HBase cluster")
public class InstallHBaseClusterCommand extends OsgiCommandSupport {

    private Tracker tracker;
    private HBase hbaseManager;

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public HBase getHbaseManager() {
        return hbaseManager;
    }

    public void setHbaseManager(HBase hbaseManager) {
        this.hbaseManager = hbaseManager;
    }

    protected Object doExecute() {

//        List<Config> configs = hbaseManager.getClusters();
//        Config config = new Config();
//        config.setClusterName(clusterName);
//        config.set
//        hbaseManager.installCluster()
//        StringBuilder sb = new StringBuilder();
//
//        for(Config config : configs) {
//            sb.append(config.getClusterName()).append("\n");
//        }
//
//        System.out.println(sb.toString());
        System.out.println("install");
        return null;
    }
}
