package io.subutai.common.environment;


import io.subutai.common.task.ImportTemplateRequest;
import io.subutai.common.task.ImportTemplateResponse;


public class PrepareTemplatesResponseCollector extends AbstractResponseCollector<ImportTemplateRequest, ImportTemplateResponse>
{
    public PrepareTemplatesResponseCollector( final String peerId )
    {
        super( peerId );
    }
}
