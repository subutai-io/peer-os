package io.subutai.common.environment;


import java.util.List;

import io.subutai.common.task.ImportTemplateRequest;
import io.subutai.common.task.ImportTemplateResponse;


public class PrepareTemplatesResponseCollector
        extends AbstractResponseCollector<ImportTemplateRequest, ImportTemplateResponse>
{
    public PrepareTemplatesResponseCollector( final String peerId )
    {
        super( peerId );
    }


    @Override
    public void onSuccess( final ImportTemplateRequest request, final ImportTemplateResponse response )
    {
        addResponse( response, String.format( "Importing %s succeeded on %s.", request.getTemplateName(),
                request.getResourceHostId() ) );
    }


    @Override
    public void onFailure( final ImportTemplateRequest request, final List<Throwable> exceptions )
    {
        addFailure( String.format( "Importing %s failed on %s.", request.getTemplateName(),
                request.getResourceHostId() ), exceptions);
    }
}
