package org.safehaus.subutai.plugin.mongodb.cli;


import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.plugin.mongodb.api.Mongo;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;

import java.util.List;


/**
 * Displays the last log entries
 */
@Command (scope = "mongodb", name = "list-clusters", description = "mydescription")
public class ListClustersCommand extends OsgiCommandSupport {

	private Mongo mongoManager;


	public Mongo getMongoManager() {
		return mongoManager;
	}


	public void setMongoManager(Mongo mongoManager) {
		this.mongoManager = mongoManager;
	}


	protected Object doExecute() {
		List<MongoClusterConfig> configList = mongoManager.getClusters();
		if (!configList.isEmpty()) {
			for (MongoClusterConfig config : configList) {
				System.out.println(config.getClusterName());
			}
		} else {
			System.out.println("No Mongo cluster");
		}

		return null;
	}
}
