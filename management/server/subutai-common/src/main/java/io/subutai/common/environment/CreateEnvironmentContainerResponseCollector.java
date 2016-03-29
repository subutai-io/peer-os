package io.subutai.common.environment;


import java.util.List;

import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.common.util.StringUtil;


public class CreateEnvironmentContainerResponseCollector extends AbstractResponseCollector<CloneRequest, CloneResponse>
{
    public CreateEnvironmentContainerResponseCollector( final String peerId )
    {
        super( peerId );
    }


    @Override
    public void onSuccess( final CloneRequest request, final CloneResponse response )
    {
        final String message = String.format( "Cloning %s succeeded on %s. [%s]", request.getContainerName(),
                request.getResourceHostId(), StringUtil.convertMillisToHHMMSS( response.getElapsedTime() ) );
        addResponse( response, message );
    }


    @Override
    public void onFailure( final CloneRequest request, final List<Throwable> exceptions )
    {
        addFailure(
                String.format( "Cloning %s failed on %s.", request.getContainerName(), request.getResourceHostId() ),
                exceptions );
    }


    public CloneResponse findByHostname( final String hostname )
    {

        for ( CloneResponse response : getResponses() )
        {
            if ( hostname.equalsIgnoreCase( response.getHostname() ) )
            {
                return response;
            }
        }

        return null;
    }
}
