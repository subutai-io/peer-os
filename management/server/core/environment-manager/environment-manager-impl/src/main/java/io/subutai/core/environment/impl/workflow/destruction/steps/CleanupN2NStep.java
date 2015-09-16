package io.subutai.core.environment.impl.workflow.destruction.steps;


import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.peer.api.LocalPeer;


public class CleanupN2NStep
{
    private final EnvironmentImpl environment;
    private final LocalPeer localPeer;


    public CleanupN2NStep( final EnvironmentImpl environment, final LocalPeer localPeer )
    {
        this.environment = environment;
        this.localPeer = localPeer;
    }


    public void execute() {}
}
