package io.subutai.common.environment;


import java.util.ArrayList;
import java.util.List;

import io.subutai.common.task.TaskResponse;
import io.subutai.common.tracker.OperationMessage;


public class ImportTemplateResponse implements TaskResponse
{
    private final String resourceHostId;
    private final String templateName;
    private final boolean succeeded;
    private List<OperationMessage> messages = new ArrayList<>();


    public ImportTemplateResponse( final String resourceHostId, final String templateName, final boolean succeeded )
    {
        this.resourceHostId = resourceHostId;
        this.templateName = templateName;
        this.succeeded = succeeded;
    }


    public String getResourceHostId()
    {
        return resourceHostId;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public boolean isSucceeded()
    {
        return succeeded;
    }


    @Override
    public List<OperationMessage> getOperationMessages()
    {
        if ( messages == null )
        {
            messages = new ArrayList<>();
        }
        return messages;
    }


    public void addFailMessage( final String msg, String description )
    {
        if ( msg == null )
        {
            throw new IllegalArgumentException( "Fail message could not be null." );
        }

        this.messages.add( new OperationMessage( msg, OperationMessage.Type.FAILED, description ) );
    }


    public void addSucceededMessage( final String msg )
    {
        if ( msg == null )
        {
            throw new IllegalArgumentException( "Message could not be null." );
        }

        this.messages.add( new OperationMessage( msg, OperationMessage.Type.SUCCEEDED, "OK" ) );
    }


    public void addSucceededMessage( final String msg, String description )
    {
        if ( msg == null )
        {
            throw new IllegalArgumentException( "Message could not be null." );
        }

        this.messages.add( new OperationMessage( msg, OperationMessage.Type.SUCCEEDED, description ) );
    }
}
