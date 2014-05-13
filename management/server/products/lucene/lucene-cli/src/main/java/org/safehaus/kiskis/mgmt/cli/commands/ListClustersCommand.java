package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.lucene.Config;
import org.safehaus.kiskis.mgmt.api.lucene.Lucene;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command(scope = "lucene", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

    private Lucene luceneManager;

    public Lucene getLuceneManager() {
        return luceneManager;
    }

    public void setLuceneManager(Lucene luceneManager) {
        this.luceneManager = luceneManager;
    }

    protected Object doExecute() {
        List<Config> configList = luceneManager.getClusters();
        if (!configList.isEmpty())
            for (Config config : configList) {
                System.out.println(config.getClusterName());
            }
        else System.out.println("No Lucene cluster");

        return null;
    }
}
