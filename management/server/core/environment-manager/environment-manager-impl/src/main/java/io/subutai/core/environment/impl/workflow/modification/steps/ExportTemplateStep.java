package io.subutai.core.environment.impl.workflow.modification.steps;


import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


public class ExportTemplateStep
{
    private final LocalEnvironment environment;
    private final String containerId;
    private final String templateName;
    private final boolean isPrivateTemplate;


    public ExportTemplateStep( final LocalEnvironment environment, final String containerId, final String templateName,
                               final boolean isPrivateTemplate )
    {
        this.environment = environment;
        this.containerId = containerId;
        this.templateName = templateName;
        this.isPrivateTemplate = isPrivateTemplate;
    }


    public void execute() throws PeerException
    {
        EnvironmentContainerHost environmentContainerHost = environment.getContainerHostById( containerId );

        final Peer peer = environmentContainerHost.getPeer();

        peer.exportTemplate( environmentContainerHost.getContainerId(), templateName, isPrivateTemplate );
    }
}
