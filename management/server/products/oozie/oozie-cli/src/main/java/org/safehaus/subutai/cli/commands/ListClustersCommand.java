package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.oozie.Config;
import org.safehaus.subutai.api.oozie.Oozie;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command(scope = "oozie", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

    private Oozie oozieManager;

    public Oozie getOozieManager() {
        return oozieManager;
    }

    public void setOozieManager(Oozie oozieManager) {
        this.oozieManager = oozieManager;
    }

    protected Object doExecute() {
        List<Config> configList = oozieManager.getClusters();
        if (!configList.isEmpty())
            for (Config config : configList) {
                System.out.println(config.getClusterName());
            }
        else System.out.println("No Oozie cluster");

        return null;
    }
}
