package io.subutai.common.environment;


import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;


public class CreateEnvironmentContainerResponseCollector extends AbstractResponseCollector<CloneRequest, CloneResponse>
{
    public CreateEnvironmentContainerResponseCollector( final String peerId )
    {
        super( peerId );
    }
}
