package org.safehaus.subutai.core.container.ui.executor;


import java.util.UUID;

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
    private UUID envId;


    public CloneCommand( ContainerManager containerManager, String hostName, String templateName, String cloneName,
                         UUID envId )
    {
        this.containerManager = containerManager;
        this.hostName = hostName;
        this.templateName = templateName;
        this.cloneName = cloneName;
        this.envId = envId;
    }


    @Override
    public void execute() throws AgentExecutionException
    {
        try
        {
            containerManager.clone(envId, hostName, templateName, cloneName );
        }
        catch ( ContainerCreateException e )
        {
            throw new AgentExecutionException( e.getMessage() );
        }
    }
}
