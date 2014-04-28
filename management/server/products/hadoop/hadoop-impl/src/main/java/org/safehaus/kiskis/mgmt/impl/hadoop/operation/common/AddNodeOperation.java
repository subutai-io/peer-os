package org.safehaus.kiskis.mgmt.impl.hadoop.operation.common;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.impl.hadoop.Tasks;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daralbaev on 22.04.14.
 */
public class AddNodeOperation {
    private final Config config;
    private List<Task> taskList;
    private Agent agent;

    public AddNodeOperation(Config config, Agent agent) {

        this.config = config;
        this.agent = agent;
        taskList = new ArrayList<Task>();

        taskList.add(Tasks.getInstallTask(agent));
        taskList.add(Tasks.getSetMastersTask(config, agent));
        taskList.add(Tasks.getExcludeNameNodeCommand(config, agent));
        taskList.add(Tasks.getExcludeTaskTrackerCommand(config, agent));
        taskList.add(Tasks.getSetDataNodeTask(config, agent));
        taskList.add(Tasks.getSetTaskTrackerTask(config, agent));
        taskList.add(Tasks.getStartNameNodeTask(agent));
        taskList.add(Tasks.getStartTaskTrackerTask(agent));
    }

    public List<Task> getTaskList() {
        return taskList;
    }
}
