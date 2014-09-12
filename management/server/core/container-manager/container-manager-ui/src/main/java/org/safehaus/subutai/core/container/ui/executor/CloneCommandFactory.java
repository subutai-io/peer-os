package org.safehaus.subutai.core.container.ui.executor;

import org.safehaus.subutai.core.container.api.ContainerManager;

/**
 * Created by timur on 9/8/14.
 */
public class CloneCommandFactory implements AgentCommandFactory {
    private ContainerManager containerManager;
    private String hostName;
    private String templateName;

    public CloneCommandFactory(ContainerManager containerManager, String hostname, String templateName) {
        this.containerManager = containerManager;
        this.hostName = hostname;
        this.templateName = templateName;
    }

    @Override
    public AgentCommand newCommand(String cloneName) {
        return new CloneCommand(containerManager, hostName, templateName, cloneName);
    }
}
