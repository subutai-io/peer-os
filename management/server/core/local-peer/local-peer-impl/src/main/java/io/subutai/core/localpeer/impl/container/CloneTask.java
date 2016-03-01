package io.subutai.core.localpeer.impl.container;


import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.command.CommandResult;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.common.task.Command;
import io.subutai.common.task.CommandBatch;
import io.subutai.common.task.TaskResponseBuilder;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.registration.api.RegistrationManager;


public class CloneTask extends AbstractTask<CloneRequest, CloneResponse>
        implements TaskResponseBuilder<CloneRequest, CloneResponse>
{
    protected static final Logger LOG = LoggerFactory.getLogger( CloneTask.class );

    private static final int CLONE_TIMEOUT = 60 * 10; // 10 min
    private static final String LINE_DELIMITER = "\n";
    private static Pattern CLONE_OUTPUT_PATTERN = Pattern.compile( "with ID (.*) successfully cloned" );

    private final int vlan;


    public CloneTask( CloneRequest request, int vlan )
    {
        super( request );
        this.vlan = vlan;
    }


    public static RegistrationManager getRegistrationManager() throws NamingException
    {
        return ServiceLocator.getServiceNoCache( RegistrationManager.class );
    }


    public CommandBatch getCommandBatch() throws Exception
    {
        CommandBatch result = new CommandBatch();

        Command cloneAction = new Command( "clone",
                Lists.newArrayList( request.getTemplateName(), request.getHostname(), "-i",
                        String.format( "\"%s %d\"", request.getIp(), this.vlan ), "-e", request.getEnvironmentId(),
                        "-t", getRegistrationManager().generateContainerTTLToken( ( CLONE_TIMEOUT + 10 ) * 1000L )
                                                      .getToken() ) );

        result.addCommand( cloneAction );

        //        for ( ContainerResource r : quota.getAllResources() )
        //        {
        //
        //            Command quotaCommand = new Command( "quota" );
        //            quotaCommand.addArgument( request.getHostname() );
        //            quotaCommand.addArgument( r.getContainerResourceType().getKey() );
        //            quotaCommand.addArgument( "-s" );
        //            quotaCommand.addArgument( r.getWriteValue() );
        //            result.addCommand( quotaCommand );
        //        }

        return result;
    }


    //    @Override
    //    public CloneResponse parse1( final CommandResult commandResult ) throws CommandResultParseException
    //    {
    //        ContainerHostInfoModel r = null;
    //        int counter = 0;
    //        String ip = null;
    //        while ( ip == null && counter < Common.WAIT_CONTAINER_CONNECTION_SEC )
    //        {
    //            try
    //            {
    //                r = findHostInfo();
    //                HostInterface i = r.getHostInterfaces().findByName( Common.DEFAULT_CONTAINER_INTERFACE );
    //                if ( !( i instanceof NullHostInterface ) )
    //                {
    //                    ip = i.getIp();
    //                }
    //                TimeUnit.SECONDS.sleep( 1 );
    //            }
    //            catch ( HostDisconnectedException | InterruptedException e )
    //            {
    //                // ignore
    //            }
    //            counter++;
    //        }
    //
    //        if ( r == null )
    //        {
    //            throw new CommandResultParseException( "Heartbeat not received from: " + request.getContainerName() );
    //        }
    //
    //        if ( ip == null )
    //        {
    //            throw new CommandResultParseException( "IP not assigned: " + request.getContainerName() );
    //        }
    //
    //        return new CloneResponse( true, request.getResourceHostId(), request.getHostname(), request
    // .getContainerName(),
    //                r.getId(), request.getIp(), request.getTemplateName(), request.getTemplateArch(),
    // getElapsedTime() );
    //    }


    //    private ContainerHostInfoModel findHostInfo() throws HostDisconnectedException
    //    {
    //        final ContainerHostInfo info = hostRegistry.getContainerHostInfoByHostname( request.getHostname() );
    //
    //        return new ContainerHostInfoModel( info );
    //    }


    @Override
    public int getTimeout()
    {
        return CLONE_TIMEOUT;
    }


    @Override
    public boolean isSequential()
    {
        return true;
    }


    @Override
    public TaskResponseBuilder<CloneRequest, CloneResponse> getResponseBuilder()
    {
        return this;
    }


    @Override
    public CloneResponse build( final CloneRequest request, final CommandResult commandResult, final long elapsedTime )
    {
        String agentId = null;

        if ( commandResult != null && commandResult.hasSucceeded() )
        {
            StringTokenizer st = new StringTokenizer( getStdErr(), LINE_DELIMITER );


            while ( st.hasMoreTokens() )
            {

                final String nextToken = st.nextToken();

                Matcher m = CLONE_OUTPUT_PATTERN.matcher( nextToken );

                LOG.debug( String.format( "Token: %s", nextToken ) );
                if ( m.find() && m.groupCount() == 1 )
                {
                    agentId = m.group( 1 );
                    break;
                }
            }
        }

        if ( agentId == null )
        {
            LOG.error( "Agent ID not found in output of subutai clone command. %s ", getStdErr() );
        }
        return new CloneResponse( request.getResourceHostId(), request.getHostname(), request.getContainerName(),
                request.getTemplateName(), request.getTemplateArch(), request.getIp(), agentId, elapsedTime,
                agentId != null ? getStdOut() : getStdErr() );
    }
}
