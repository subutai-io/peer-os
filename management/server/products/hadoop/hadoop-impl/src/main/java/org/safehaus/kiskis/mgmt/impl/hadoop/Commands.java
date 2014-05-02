package org.safehaus.kiskis.mgmt.impl.hadoop;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentRequestBuilder;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by daralbaev on 02.04.14.
 */
public class Commands {

    public static Command getInstallCommand(Config config) {
        return HadoopImpl.getCommandRunner().createCommand(
                new RequestBuilder("sleep 10;" +
                        "apt-get --force-yes --assume-yes install ksks-hadoop")
                        .withTimeout(180),
                new HashSet<Agent>(config.getAllNodes())
        );
    }

    public static Command getClearMastersCommand(Config config) {
        return HadoopImpl.getCommandRunner().createCommand(
                new RequestBuilder(". /etc/profile && " +
                        "hadoop-master-slave.sh masters clear"),
                Sets.newHashSet(config.getNameNode())
        );
    }

    public static Command getClearSlavesCommand(Config config) {
        return HadoopImpl.getCommandRunner().createCommand(
                new RequestBuilder(". /etc/profile && " +
                        "hadoop-master-slave.sh slaves clear"),
                Sets.newHashSet(config.getNameNode(), config.getJobTracker())
        );
    }

    public static Command getSetMastersCommand(Config config) {
        return HadoopImpl.getCommandRunner().createCommand(
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
                new RequestBuilder(cmd.toString()),
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
                new RequestBuilder(cmd.toString()),
                Sets.newHashSet(config.getJobTracker())
        );
    }

    public static Request getRemoveSlaveCommand(Agent agent) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(
                ". /etc/profile && " +
                        "hadoop-master-slave.sh slaves clear %s", agent.getHostname()
        ));
        return req;
    }

    public static Request getExcludeDataNodeCommand(Agent agent) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(
                        ". /etc/profile && " +
                                "hadoop-master-slave.sh dfs.exclude clear %s", agent.getHostname()
                )
        );
        return req;
    }

    public static Command getExcludeDataNodeCommand(Config config, Agent agent) {
        return HadoopImpl.getCommandRunner().createCommand(
                new RequestBuilder(String.format(
                        ". /etc/profile && " +
                                "hadoop-master-slave.sh dfs.exclude clear %s", agent.getHostname()
                )),
                Sets.newHashSet(config.getNameNode())
        );
    }

    public static Request getExcludeTaskTrackerCommand(Agent agent) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(
                        ". /etc/profile && " +
                                "hadoop-master-slave.sh mapred.exclude clear %s", agent.getHostname()
                )
        );
        return req;
    }

    public static Request getIncludeDataNodeCommand(Agent agent) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(
                        ". /etc/profile && " +
                                "hadoop-master-slave.sh dfs.exclude %s", agent.getHostname()
                )
        );
        return req;
    }

    public static Request getIncludeTaskTrackerCommand(Agent agent) {
        Request req = getRequestTemplate();
        req.setProgram(String.format(
                        ". /etc/profile && " +
                                "hadoop-master-slave.sh mapred.exclude %s", agent.getHostname()
                )
        );
        return req;
    }

    public static Command getFormatNameNodeCommand(Config config) {
        return HadoopImpl.getCommandRunner().createCommand(
                new RequestBuilder(". /etc/profile && " +
                        "hadoop namenode -format"),
                Sets.newHashSet(config.getNameNode())
        );
    }

    public static Request getRefreshNameNodeCommand() {
        Request req = getRequestTemplate();
        req.setProgram(
                ". /etc/profile && " +
                        "hadoop dfsadmin -refreshNodes"
        );
        return req;
    }

    public static Request getRefreshJobTrackerCommand() {
        Request req = getRequestTemplate();
        req.setProgram(
                ". /etc/profile && " +
                        "hadoop mradmin -refreshNodes"
        );
        return req;
    }

    public static Request getStartNameNodeCommand() {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && " +
                        "hadoop-daemons.sh start datanode"
        );
        req.setTimeout(20);
        return req;
    }

    public static Request getStartTaskTrackerCommand() {
        Request req = getRequestTemplate();
        req.setProgram(". /etc/profile && " +
                        "hadoop-daemons.sh start tasktracker"
        );
        req.setTimeout(20);
        return req;
    }

    public static Command getNameNodeCommand(Agent agent, String command) {
        return HadoopImpl.getCommandRunner().createCommand(
                new RequestBuilder(String.format("service hadoop-dfs %s", command))
                        .withTimeout(20),
                Sets.newHashSet(agent)
        );
    }

    public static Command getJobTrackerCommand(Agent agent, String command) {
        return HadoopImpl.getCommandRunner().createCommand(
                new RequestBuilder(String.format("service hadoop-mapred %s", command))
                        .withTimeout(20),
                Sets.newHashSet(agent)
        );
    }
}
