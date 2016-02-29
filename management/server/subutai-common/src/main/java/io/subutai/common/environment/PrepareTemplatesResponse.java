package io.subutai.common.environment;


import io.subutai.common.task.ImportTemplateRequest;
import io.subutai.common.task.ImportTemplateResponse;


public class PrepareTemplatesResponse extends AbstractGroupResponse<ImportTemplateRequest, ImportTemplateResponse>
{
    public PrepareTemplatesResponse( final String peerId )
    {
        super( peerId );
    }
}
