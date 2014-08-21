package org.safehaus.subutai.plugin.flume.cli;

import java.util.List;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.plugin.flume.api.Flume;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;

/**
 * Displays the last log entries
 */
@Command(scope = "flume", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

    private Flume flumeManager;

    public Flume getFlumeManager() {
        return flumeManager;
    }

    public void setFlumeManager(Flume flumeManager) {
        this.flumeManager = flumeManager;
    }

    @Override
    protected Object doExecute() {

        List<FlumeConfig> configList = flumeManager.getClusters();
        if(!configList.isEmpty())
            for(FlumeConfig config : configList) {
                System.out.println(config.getClusterName());
            }
        else System.out.println("No Flume clusters");

        return null;
    }
}
