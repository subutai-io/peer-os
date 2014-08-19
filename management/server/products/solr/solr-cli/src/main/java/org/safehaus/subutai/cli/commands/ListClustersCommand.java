package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.solr.Config;
import org.safehaus.subutai.api.solr.Solr;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "solr", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

	private Solr solrManager;

	public Solr getSolrManager() {
		return solrManager;
	}

	public void setSolrManager(Solr solrManager) {
		this.solrManager = solrManager;
	}

	protected Object doExecute() {
		List<Config> configList = solrManager.getClusters();
		if (!configList.isEmpty())
			for (Config config : configList) {
				System.out.println(config.getClusterName());
			}
		else System.out.println("No Solr cluster");

		return null;
	}
}
