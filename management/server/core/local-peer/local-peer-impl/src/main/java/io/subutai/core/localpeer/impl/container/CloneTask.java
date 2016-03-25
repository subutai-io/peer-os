package io.subutai.core.localpeer.impl.container;


import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.command.CommandException;
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

    private static final int CLONE_TIMEOUT = 60 * 5; // 5 min
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
        CommandBatch result = new CommandBatch( CommandBatch.Type.STANDARD );

        Command cloneAction = new Command( "clone",
                Lists.newArrayList( request.getTemplateName(), request.getHostname(), "-i",
                        String.format( "\"%s %d\"", request.getIp(), this.vlan ), "-e", request.getEnvironmentId(),
                        "-t", getRegistrationManager().generateContainerTTLToken( ( CLONE_TIMEOUT + 10 ) * 1000L )
                                                      .getToken() ) );

        result.addCommand( cloneAction );

        return result;
    }


    @Override
    public int getTimeout()
    {
        return CLONE_TIMEOUT;
    }


    @Override
    public boolean isSequential()
    {
        return false;
    }


    @Override
    public TaskResponseBuilder<CloneRequest, CloneResponse> getResponseBuilder()
    {
        return this;
    }


    @Override
    public CloneResponse build( final CloneRequest request, final CommandResult commandResult, final long elapsedTime )
            throws Exception
    {
        String containerId = null;

        if ( commandResult != null && commandResult.hasSucceeded() )
        {
            // why stdErr()?
            StringTokenizer st = new StringTokenizer( getStdErr(), LINE_DELIMITER );


            while ( st.hasMoreTokens() )
            {

                final String nextToken = st.nextToken();

                Matcher m = CLONE_OUTPUT_PATTERN.matcher( nextToken );

                LOG.debug( String.format( "Token: %s", nextToken ) );
                if ( m.find() && m.groupCount() == 1 )
                {
                    containerId = m.group( 1 );
                    break;
                }
            }
        }

        if ( containerId == null )
        {
            LOG.error( "Agent ID not found in output of subutai clone command." );
            throw new CommandException( "Agent ID not found in output of subutai clone command." );
        }
        return new CloneResponse( request.getResourceHostId(), request.getHostname(), request.getContainerName(),
                request.getTemplateName(), request.getTemplateArch(), request.getIp(), containerId, elapsedTime );
    }
}
