package org.safehaus.subutai.impl.hadoop;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.shared.protocol.Agent;

/**
 * Created by daralbaev on 02.04.14.
 */
public class Commands {

	public static Command getInstallCommand(Config config) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Installing hadoop deb package",
				new RequestBuilder("sleep 20;" +
						"apt-get --force-yes --assume-yes install ksks-hadoop")
						.withTimeout(180),
				Sets.newHashSet(config.getAllNodes())
		);
	}

	public static Command getInstallCommand(Agent agent) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Installing hadoop deb package",
				new RequestBuilder("sleep 20;" +
						"apt-get --force-yes --assume-yes install ksks-hadoop")
						.withTimeout(180),
				Sets.newHashSet(agent)
		);
	}

	public static Command getClearMastersCommand(Config config) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Clear master nodes for NameNode",
				new RequestBuilder(". /etc/profile && " +
						"hadoop-master-slave.sh masters clear"),
				Sets.newHashSet(config.getNameNode())
		);
	}

	public static Command getClearSlavesCommand(Config config) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Clear slave nodes for NameNode and JobTracker",
				new RequestBuilder(". /etc/profile && " +
						"hadoop-master-slave.sh slaves clear"),
				Sets.newHashSet(config.getNameNode(), config.getJobTracker())
		);
	}

	public static Command getSetMastersCommand(Config config) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Set masters for nodes",
				new RequestBuilder(". /etc/profile && " +
						"hadoop-configure.sh")
						.withCmdArgs(Lists.newArrayList(
								String.format("%s:%d", config.getNameNode().getHostname(), Config.NAME_NODE_PORT),
								String.format("%s:%d", config.getJobTracker().getHostname(), Config.JOB_TRACKER_PORT),
								String.format("%d", config.getReplicationFactor())
						)),
				Sets.newHashSet(config.getAllNodes())
		);
	}

	public static Command getSetMastersCommand(Config config, Agent agent) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Set masters for nodes",
				new RequestBuilder(". /etc/profile && " +
						"hadoop-configure.sh")
						.withCmdArgs(Lists.newArrayList(
								String.format("%s:%d", config.getNameNode().getHostname(), Config.NAME_NODE_PORT),
								String.format("%s:%d", config.getJobTracker().getHostname(), Config.JOB_TRACKER_PORT),
								String.format("%d", config.getReplicationFactor())
						)),
				Sets.newHashSet(agent)
		);
	}

	public static Command getAddSecondaryNamenodeCommand(Config config) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Set Secondary NameNode master for NameNode",
				new RequestBuilder(String.format(
						". /etc/profile && " +
								"hadoop-master-slave.sh masters %s",
						config.getSecondaryNameNode().getHostname()
				)),
				Sets.newHashSet(config.getNameNode())
		);
	}

	public static Command getSetDataNodeCommand(Config config) {

		StringBuilder cmd = new StringBuilder();
		for (Agent agent : config.getDataNodes()) {
			cmd.append(String.format(
					". /etc/profile && " +
							"hadoop-master-slave.sh slaves %s; ",
					agent.getHostname()
			));
		}

		return HadoopImpl.getCommandRunner().createCommand(
				"Set DataNodes for NameNode",
				new RequestBuilder(cmd.toString()),
				Sets.newHashSet(config.getNameNode())
		);
	}

	public static Command getSetDataNodeCommand(Config config, Agent agent) {

		return HadoopImpl.getCommandRunner().createCommand(
				"Set DataNodes for NameNode",
				new RequestBuilder(String.format(
						". /etc/profile && " +
								"hadoop-master-slave.sh slaves %s; ",
						agent.getHostname()
				)),
				Sets.newHashSet(config.getNameNode())
		);
	}

	public static Command getSetTaskTrackerCommand(Config config) {

		StringBuilder cmd = new StringBuilder();
		for (Agent agent : config.getTaskTrackers()) {
			cmd.append(String.format(
					". /etc/profile && " +
							"hadoop-master-slave.sh slaves %s; ",
					agent.getHostname()
			));
		}

		return HadoopImpl.getCommandRunner().createCommand(
				"Set TaskTrackers for JobTracker",
				new RequestBuilder(cmd.toString()),
				Sets.newHashSet(config.getJobTracker())
		);
	}

	public static Command getSetTaskTrackerCommand(Config config, Agent agent) {

		return HadoopImpl.getCommandRunner().createCommand(
				"Set TaskTrackers for JobTracker",
				new RequestBuilder(String.format(
						". /etc/profile && " +
								"hadoop-master-slave.sh slaves %s; ",
						agent.getHostname()
				)),
				Sets.newHashSet(config.getJobTracker())
		);
	}

	public static Command getRemoveDataNodeCommand(Config config, Agent agent) {

		return HadoopImpl.getCommandRunner().createCommand(
				"Remove DataNode from NameNode",
				new RequestBuilder(String.format(
						". /etc/profile && " +
								"hadoop-master-slave.sh slaves clear %s", agent.getHostname()
				)),
				Sets.newHashSet(config.getNameNode())
		);
	}

	public static Command getRemoveTaskTrackerCommand(Config config, Agent agent) {

		return HadoopImpl.getCommandRunner().createCommand(
				"Remove TaskTrackers from JobTracker",
				new RequestBuilder(String.format(
						". /etc/profile && " +
								"hadoop-master-slave.sh slaves clear %s", agent.getHostname()
				)),
				Sets.newHashSet(config.getJobTracker())
		);
	}

	public static Command getExcludeDataNodeCommand(Config config, Agent agent) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Remove DataNode from dfs blacklist",
				new RequestBuilder(String.format(
						". /etc/profile && " +
								"hadoop-master-slave.sh dfs.exclude clear %s", agent.getHostname()
				)),
				Sets.newHashSet(config.getNameNode())
		);
	}

	public static Command getExcludeTaskTrackerCommand(Config config, Agent agent) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Remove TaskTracker from mapred blacklist",
				new RequestBuilder(String.format(
						". /etc/profile && " +
								"hadoop-master-slave.sh mapred.exclude clear %s", agent.getHostname()
				)),
				Sets.newHashSet(config.getJobTracker())
		);
	}

	public static Command getIncludeDataNodeCommand(Config config, Agent agent) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Add DataNode to dfs blacklist",
				new RequestBuilder(String.format(
						". /etc/profile && " +
								"hadoop-master-slave.sh dfs.exclude %s", agent.getHostname()
				)),
				Sets.newHashSet(config.getNameNode())
		);
	}

	public static Command getIncludeTaskTrackerCommand(Config config, Agent agent) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Add TaskTracker to mapred blacklist",
				new RequestBuilder(String.format(
						". /etc/profile && " +
								"hadoop-master-slave.sh mapred.exclude %s", agent.getHostname()
				)),
				Sets.newHashSet(config.getJobTracker())
		);
	}

	public static Command getFormatNameNodeCommand(Config config) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Format NameNode before first start",
				new RequestBuilder(". /etc/profile && " +
						"hadoop namenode -format"),
				Sets.newHashSet(config.getNameNode())
		);
	}

	public static Command getRefreshNameNodeCommand(Config config) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Refresh NameNode",
				new RequestBuilder(". /etc/profile && " +
						"hadoop dfsadmin -refreshNodes"),
				Sets.newHashSet(config.getNameNode())
		);
	}

	public static Command getRefreshJobTrackerCommand(Config config) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Refresh JobTracker",
				new RequestBuilder(". /etc/profile && " +
						"hadoop mradmin -refreshNodes"),
				Sets.newHashSet(config.getJobTracker())
		);
	}

	public static Command getStartNameNodeCommand(Agent agent) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Start DataNode",
				new RequestBuilder(". /etc/profile && " +
						"hadoop-daemons.sh start datanode")
						.withTimeout(20),
				Sets.newHashSet(agent)
		);
	}

	public static Command getStartTaskTrackerCommand(Agent agent) {
		return HadoopImpl.getCommandRunner().createCommand(
				"Start TaskTracker",
				new RequestBuilder(". /etc/profile && " +
						"hadoop-daemons.sh start tasktracker")
						.withTimeout(20),
				Sets.newHashSet(agent)
		);
	}

	public static Command getNameNodeCommand(Agent agent, String command) {
		return HadoopImpl.getCommandRunner().createCommand(
				String.format("Execute NameNode/SecondaryNameNode/DataNode command %s", command),
				new RequestBuilder(String.format("sleep 10; service hadoop-dfs %s &", command))
						.withTimeout(20),
				Sets.newHashSet(agent)
		);
	}

	public static Command getJobTrackerCommand(Agent agent, String command) {
		return HadoopImpl.getCommandRunner().createCommand(
				String.format("Execute JobTracker/TaskTracker command %s", command),
				new RequestBuilder(String.format("sleep 10; service hadoop-mapred %s &", command))
						.withTimeout(20),
				Sets.newHashSet(agent)
		);
	}
}
