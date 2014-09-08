package org.safehaus.subutai.ui.containermanager.clone;

import org.safehaus.subutai.api.containermanager.ContainerCreateException;
import org.safehaus.subutai.api.containermanager.ContainerManager;

/**
 * Created by timur on 9/8/14.
 */
public class CloneCommand extends AbstractCommand implements AgentCommand {
    private ContainerManager containerManager;


    public CloneCommand(ContainerManager containerManager, String hostName, String templateName, String cloneName) {
        super(hostName, templateName, cloneName);
        this.containerManager = containerManager;
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
