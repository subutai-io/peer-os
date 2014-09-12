package org.safehaus.subutai.core.container.ui.executor;

import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.container.api.ContainerManager;

/**
 * Created by timur on 9/8/14.
 */
public class CloneCommand implements AgentCommand {
    private ContainerManager containerManager;
    private String hostName;
    private String templateName;
    private String cloneName;


    public CloneCommand(ContainerManager containerManager, String hostName, String templateName, String cloneName) {
        this.containerManager = containerManager;
        this.hostName = hostName;
        this.templateName = templateName;
        this.cloneName = cloneName;
    }

    @Override
    public void execute() throws AgentExecutionException {
        try {
            containerManager.clone(hostName, templateName, cloneName);
        } catch (ContainerCreateException e) {
            throw new AgentExecutionException(e.getMessage());
        }
    }
}
