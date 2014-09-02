package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.hive.query.Config;
import org.safehaus.subutai.api.hive.query.HiveQuery;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Displays the last log entries
 */
@Command (scope = "hivequery", name = "run-query", description = "Run hive query")
public class RunQueryCommand extends OsgiCommandSupport {
	@Argument (index = 0, name = "agentHostname", description = "Hostname of agent", required = true, multiValued = false)
	String agentHostname = null;
	@Argument (index = 1, name = "query", description = "The sql of hive query.", required = true, multiValued = false)
	String query = null;
	private HiveQuery manager;
	private Tracker tracker;
	private ExecutorService executor;

	protected Object doExecute() throws Exception {
		final UUID trackID = manager.run(agentHostname, query);
		executor = Executors.newCachedThreadPool();

		executor.execute(new Runnable() {

			public void run() {
				long start = System.currentTimeMillis();
				while (!Thread.interrupted()) {
					ProductOperationView po = getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
					if (po != null) {
						if (po.getState() != ProductOperationState.RUNNING) {
							System.out.println(po.getLog());
							System.out.println("Query finished.");
							break;
						}
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						break;
					}
					if (System.currentTimeMillis() - start > (30 + 3) * 1000) {
						break;
					}
				}
			}
		});

		return null;
	}

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}

	public HiveQuery getManager() {
		return manager;
	}

	public void setManager(HiveQuery manager) {
		this.manager = manager;
	}
}
