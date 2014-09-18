package org.safehaus.subutai.core.container.ui.executor;

import java.util.UUID;

import org.safehaus.subutai.core.container.api.ContainerManager;

/**
 * Created by timur on 9/8/14.
 */
public class CloneCommandFactory implements AgentCommandFactory {
    private ContainerManager containerManager;
    private String hostName;
    private String templateName;
    private UUID envId;

    public CloneCommandFactory(ContainerManager containerManager, UUID envId, String hostname, String templateName) {
        this.containerManager = containerManager;
        this.hostName = hostname;
        this.templateName = templateName;
        this.envId = envId;
    }

    @Override
    public AgentCommand newCommand(String cloneName) {
        return new CloneCommand(containerManager, hostName, templateName, cloneName, envId);
    }
}
