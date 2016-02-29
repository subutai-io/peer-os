package io.subutai.common.environment;


import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;


public class CreateEnvironmentContainerGroupResponse extends AbstractGroupResponse<CloneRequest, CloneResponse>
{
    public CreateEnvironmentContainerGroupResponse( final String peerId )
    {
        super( peerId );
    }
}
