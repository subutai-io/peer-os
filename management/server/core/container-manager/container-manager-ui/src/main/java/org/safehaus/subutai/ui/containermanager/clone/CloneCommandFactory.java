package org.safehaus.subutai.ui.containermanager.clone;

import org.safehaus.subutai.api.containermanager.ContainerManager;

/**
 * Created by timur on 9/8/14.
 */
public class CloneCommandFactory implements AgentCommandFactory{
    private ContainerManager containerManager;
//    private String hostName;
//    private String templateName;
    private AgentExecutor agentExecutor;

    public CloneCommandFactory(ContainerManager containerManager, AgentExecutor agentExecutor) {
        this.containerManager = containerManager;
        this.agentExecutor = agentExecutor;
//        this.hostName = hostName;
//        this.templateName = templateName;
    }

    @Override
    public AgentCommand newCommand(String cloneName) {
        return new CloneCommand(containerManager, agentExecutor.getHostName(), agentExecutor.getTemplateName(), cloneName);
    }
}
