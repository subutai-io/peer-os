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

    private static final String LINE_DELIMITER = "\n";
    private static Pattern CLONE_OUTPUT_PATTERN = Pattern.compile( "with ID (.*) successfully cloned" );

    private String resourceHostId;
    private String hostname;
    private String templateName;
    private HostArchitecture templateArch;
    private String containerName;
    private String ip;
    private String agentId;
    private long elapsedTime;


    public CloneResponse( final String resourceHostId, final String hostname, final String containerName, final String templateName,
                          final HostArchitecture templateArch,  final String ip,
                          final String agentId, final long elapsedTime )
    {
        this.resourceHostId = resourceHostId;
        this.hostname = hostname;
        this.templateName = templateName;
        this.templateArch = templateArch;
        this.containerName = containerName;
        this.ip = ip;
        this.agentId = agentId;
        this.elapsedTime = elapsedTime;
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


    public String getTemplateName()
    {
        return templateName;
    }


    public HostArchitecture getTemplateArch()
    {
        return templateArch;
    }


    //    @Override
    //    public void processCommandResult( final CloneRequest request, final CommandResult commandResult,
    //                                      final long elapsedTime )
    //    {
    //        StringTokenizer st = new StringTokenizer( commandResult.getStdOut(), LINE_DELIMITER );
    //
    //        String agentId = null;
    //        while ( st.hasMoreTokens() )
    //        {
    //
    //            final String nextToken = st.nextToken();
    //
    //            Matcher m = CLONE_OUTPUT_PATTERN.matcher( nextToken );
    //
    //            //             LOG.debug( String.format( "Token: %s", nextToken ) );
    //            if ( m.find() && m.groupCount() == 1 )
    //            {
    //                agentId = m.group( 1 );
    //                break;
    //            }
    //        }
    //
    //        if ( agentId == null )
    //        {
    //            succeeded = false;
    //            //             LOG.error( "Agent ID not found in output of subutai clone command. %s ", commandResult
    //            // .getStdOut() );
    //            //             new CloneResponse( false, request.getResourceHostId(), request.getHostname(), request
    //            // .getContainerName(),
    //            //                     null, request.getIp(), request.getTemplateName(), request.getTemplateArch(),
    //            // getElapsedTime() );
    //        }
    //        else
    //        {
    //            this.resourceHostId = request.getResourceHostId();
    //            this.hostname = request.getHostname();
    //            this.agentId = agentId;
    //            this.ip = request.getIp();
    //            this.succeeded = true;
    //        }
    //
    //        this.elapsedTime = elapsedTime;
    //        //         return new CloneResponse( true, request.getResourceHostId(), request.getHostname(), request
    //        // .getContainerName(),
    //        //                 agentId, request.getIp(), request.getTemplateName(), request.getTemplateArch(),
    //        // getElapsedTime() );
    //    }


    @Override
    public boolean hasSucceeded()
    {
        return agentId != null;
    }


    @Override
    public String getLog()
    {
        return hasSucceeded() ? String.format( "Cloning %s succeeded.", containerName ) :
               String.format( "Cloning %s failed.", containerName );
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
