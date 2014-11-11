package org.safehaus.subutai.core.lxc.quota.impl;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.lxc.quota.api.QuotaException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Created by talas on 10/7/14.
 */
public class QuotaManagerImpl implements QuotaManager
{

    private CommandRunner commandRunner;
    private static Logger LOGGER = LoggerFactory.getLogger( QuotaManagerImpl.class );


    public QuotaManagerImpl( CommandRunner commandRunner )
    {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );
        this.commandRunner = commandRunner;
    }


    @Override
    public void setQuota( final String containerName, final QuotaEnum parameter, final String newValue,
                          final Agent agent ) throws QuotaException
    {
        Preconditions.checkNotNull( containerName, "Target containerName is null." );
        Preconditions.checkNotNull( parameter, "Parameter is null." );
        Preconditions.checkNotNull( newValue, "New value to set is null." );
        Preconditions.checkNotNull( agent, "Host is null." );

        String precomputedString =
                String.format( "lxc-cgroup -n %s %s %s", containerName, parameter.getKey(), newValue );
        Command command =
                commandRunner.createCommand( new RequestBuilder( precomputedString ), Sets.newHashSet( agent ) );
        runCommand( command, false, agent, parameter, containerName );
    }


    @Override
    public String getQuota( final String containerName, final QuotaEnum parameter, final Agent agent )
            throws QuotaException
    {
        Preconditions.checkNotNull( containerName, "Target containerName is null." );
        Preconditions.checkNotNull( parameter, "Parameter is null." );
        Preconditions.checkNotNull( agent, "Host is null." );

        String precomputedString = String.format( "lxc-cgroup -n %s %s", containerName, parameter.getKey() );
        Command command =
                commandRunner.createCommand( new RequestBuilder( precomputedString ), Sets.newHashSet( agent ) );
        return runCommand( command, true, agent, parameter, containerName );
    }


    private String runCommand( Command command, boolean givesOutput, Agent host, QuotaEnum parameter,
                               String containerName ) throws QuotaException
    {
        try
        {
            command.execute();
            if ( !command.hasSucceeded() )
            {
                if ( command.hasCompleted() )
                {
                    AgentResult agentResult = command.getResults().get( host.getUuid() );
                    throw new QuotaException( String.format(
                            "Error while performing [lxc-cgroup -n %1$s %2$s]: %3$s%n%4$s, exit code %5$s",
                            containerName, parameter.getKey(), agentResult.getStdOut(), agentResult.getStdErr(),
                            agentResult.getExitCode() ) );
                }
                else
                {
                    throw new QuotaException(
                            String.format( "Error while performing [lxc-cgroup -n %1$s %2$s]: Command timed out",
                                    containerName, parameter.getKey() ) );
                }
            }
            else if ( givesOutput )
            {
                AgentResult agentResult = command.getResults().get( host.getUuid() );
                LOGGER.info( agentResult.getStdOut() );
                return agentResult.getStdOut();
            }
        }
        catch ( CommandException e )
        {
            LOGGER.error( "Error executing lxc-cgroup command.", e.getMessage() );
        }
        return "";
    }
}
