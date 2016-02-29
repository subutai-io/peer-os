package io.subutai.common.task;


import java.util.ArrayList;
import java.util.List;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.task.TaskRequest;
import io.subutai.common.task.TaskResponse;
import io.subutai.common.tracker.OperationMessage;


public class CloneResponse implements TaskResponse
{
    private final String resourceHostId;
    private final String hostname;
    private final String templateName;
    private final HostArchitecture templateArch;
    private final String containerName;
    private final String ip;
    private final String agentId;
    private List<OperationMessage> messages = new ArrayList<>();


    public CloneResponse( final String resourceHostId, final String hostname, final String containerName,
                          final String agentId, final String ip, final String templateName,
                          final HostArchitecture templateArch )
    {
        this.resourceHostId = resourceHostId;
        this.hostname = hostname;
        this.containerName = containerName;
        this.ip = ip;
        this.agentId = agentId;
        this.templateName = templateName;
        this.templateArch = templateArch;
    }


    public String getResourceHostId()
    {
        return resourceHostId;
    }


    public String getHostname()
    {
        return hostname;
    }


    public String getContainerName()
    {
        return containerName;
    }


    public String getIp()
    {
        return ip;
    }


    public String getAgentId()
    {
        return agentId;
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


    public String getTemplateName()
    {
        return templateName;
    }


    public HostArchitecture getTemplateArch()
    {
        return templateArch;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "CloneResponse{" );
        sb.append( "resourceHostId='" ).append( resourceHostId ).append( '\'' );
        sb.append( ", hostname='" ).append( hostname ).append( '\'' );
        sb.append( ", templateName='" ).append( templateName ).append( '\'' );
        sb.append( ", templateArch=" ).append( templateArch );
        sb.append( ", containerName='" ).append( containerName ).append( '\'' );
        sb.append( ", ip='" ).append( ip ).append( '\'' );
        sb.append( ", agentId='" ).append( agentId ).append( '\'' );
        sb.append( ", messages=" ).append( messages );
        sb.append( '}' );
        return sb.toString();
    }
}
