package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.operation;

import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.HadoopConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.Tasks;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.taskrunner.Operation;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 1/31/14
 * Time: 10:08 PM
 */
public class InstallHadoopOperation extends Operation {
    private final HadoopConfig config;


    public InstallHadoopOperation(HadoopConfig config) {
        super("Install Hadoop cluster");

        this.config = config;
        config.setCluster();

        addTask(Tasks.getInstallTask(config.getAllNodes()));

        addTask(Tasks.getSetMastersTask(config.getAllNodes(), config.getNameNode(), config.getJobTracker(), config.getReplicationFactor()));

        addTask(Tasks.getClearSecondaryNameNodeTask(config.getNameNode()));

        addTask(Tasks.getSetSecondaryNameNodeTask(config.getNameNode(), config.getsNameNode()));

        addTask(Tasks.getClearDataNodesTask(config.getNameNode()));
        for(Agent agent : config.getDataNodes()){
            addTask(Tasks.getSetDataNodesTask(config.getNameNode(), agent));
        }

        addTask(Tasks.getClearTaskTrackersTask(config.getJobTracker()));
        for(Agent agent: config.getTaskTrackers()){
            addTask(Tasks.getSetTaskTrackersTask(config.getJobTracker(), agent));
        }

        addTask(Tasks.getFormatMasterTask(config.getNameNode()));

        addTask(Tasks.getSetSSHTask(config.getAllNodes()));
    }


}
