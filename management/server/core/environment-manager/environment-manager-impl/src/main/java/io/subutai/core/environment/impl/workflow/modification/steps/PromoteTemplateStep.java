package io.subutai.core.environment.impl.workflow.modification.steps;


import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


public class PromoteTemplateStep
{
    private final LocalEnvironment environment;
    private final String containerId;
    private final String templateName;


    public PromoteTemplateStep( final LocalEnvironment environment, final String containerId,
                                final String templateName )
    {
        this.environment = environment;
        this.containerId = containerId;
        this.templateName = templateName;
    }


    public void execute() throws PeerException
    {
        EnvironmentContainerHost environmentContainerHost = environment.getContainerHostById( containerId );

        final Peer peer = environmentContainerHost.getPeer();

        peer.promoteTemplate( environmentContainerHost.getContainerId(), templateName );
    }
}
