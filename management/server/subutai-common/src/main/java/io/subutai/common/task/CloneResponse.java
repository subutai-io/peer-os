package io.subutai.common.task;


import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultParseException;
import io.subutai.common.host.HostArchitecture;


public class CloneResponse implements TaskResponse
{
    protected static final Logger LOG = LoggerFactory.getLogger( CloneResponse.class );

    private String resourceHostId;
    private String hostname;
    private String templateName;
    private HostArchitecture templateArch;
    private String containerName;
    private String ip;
    private String agentId;
    private long elapsedTime;
    private String description;


    public CloneResponse( final String resourceHostId, final String hostname, final String containerName,
                          final String templateName, final HostArchitecture templateArch, final String ip,
                          final String agentId, final long elapsedTime, final String description )
    {
        this.resourceHostId = resourceHostId;
        this.hostname = hostname;
        this.templateName = templateName;
        this.templateArch = templateArch;
        this.containerName = containerName;
        this.ip = ip;
        this.agentId = agentId;
        this.elapsedTime = elapsedTime;
        this.description = description;
    }


    @Override
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


    public String getTemplateName()
    {
        return templateName;
    }


    public HostArchitecture getTemplateArch()
    {
        return templateArch;
    }


    @Override
    public String getDescription()
    {
        return description;
    }


    @Override
    public boolean hasSucceeded()
    {
        return agentId != null;
    }


    @Override
    public String getLog()
    {
        return hasSucceeded() ? String.format( "Cloning '%s' using '%s' succeeded.", containerName, templateName ) :
               String.format( "Cloning %s failed on %s.", containerName, getResourceHostId() );
    }


    @Override
    public long getElapsedTime()
    {
        return elapsedTime;
    }


    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder( "CloneResponse{" );
        sb.append( "resourceHostId='" ).append( resourceHostId ).append( '\'' );
        sb.append( ", hostname='" ).append( hostname ).append( '\'' );
        sb.append( ", templateName='" ).append( templateName ).append( '\'' );
        sb.append( ", templateArch=" ).append( templateArch );
        sb.append( ", containerName='" ).append( containerName ).append( '\'' );
        sb.append( ", ip='" ).append( ip ).append( '\'' );
        sb.append( ", agentId='" ).append( agentId ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}
