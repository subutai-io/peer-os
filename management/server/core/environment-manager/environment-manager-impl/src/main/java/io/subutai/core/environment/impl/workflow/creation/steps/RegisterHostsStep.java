package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class RegisterHostsStep
{
    private static final Logger LOG = LoggerFactory.getLogger( RegisterHostsStep.class );

    private final EnvironmentImpl environment;
    private final TrackerOperation trackerOperation;
    private CommandUtil commandUtil = new CommandUtil();


    public RegisterHostsStep( final EnvironmentImpl environment, final TrackerOperation trackerOperation )
    {
        this.environment = environment;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentManagerException
    {
        Set<Host> hosts = Sets.newHashSet();
        hosts.addAll( environment.getContainerHosts() );
        if ( hosts.size() > 1 )
        {
            registerHosts( Common.DEFAULT_DOMAIN_NAME, hosts );
        }
    }


    protected void registerHosts( String localDomain, Set<Host> hosts ) throws EnvironmentManagerException
    {

        Map<Host, CommandResult> results =
                commandUtil.executeParallelSilent( getAddIpHostToEtcHostsCommand( localDomain, hosts ), hosts );

        Set<Host> succeededHosts = Sets.newHashSet();
        for ( Map.Entry<Host, CommandResult> resultEntry : results.entrySet() )
        {
            CommandResult result = resultEntry.getValue();
            Host host = resultEntry.getKey();

            if ( result.hasSucceeded() )
            {
                succeededHosts.add( host );
            }
            else
            {
                LOG.debug( String.format( "Error: %s, Exit Code %d", result.getStdErr(), result.getExitCode() ) );
            }
        }

        hosts.removeAll( succeededHosts );

        for ( Host failedHost : hosts )
        {
            trackerOperation.addLog( String.format( "Host registration failed on host %s", failedHost.getHostname() ) );
        }

        if ( !hosts.isEmpty() )
        {
            throw new EnvironmentManagerException( "Failed to register all hosts" );
        }
    }


    public RequestBuilder getAddIpHostToEtcHostsCommand( String domainName, Set<Host> containerHosts )
    {
        StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
        StringBuilder appendHosts = new StringBuilder();

        for ( Host host : containerHosts )
        {
            String ip = host.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp();
            String hostname = host.getHostname();
            cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
            appendHosts.append( "/bin/echo '" ).
                    append( ip ).append( " " ).
                               append( hostname ).append( "." ).append( domainName ).
                               append( " " ).append( hostname ).
                               append( "' >> '/etc/hosts'; " );
        }

        if ( cleanHosts.length() > 0 )
        {
            //drop pipe | symbol
            cleanHosts.setLength( cleanHosts.length() - 1 );
            cleanHosts.insert( 0, "egrep -v '" );
            cleanHosts.append( "' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;" );
            appendHosts.insert( 0, cleanHosts );
        }

        appendHosts.append( "/bin/echo '127.0.0.1 localhost " ).append( "' >> '/etc/hosts';" );

        return new RequestBuilder( appendHosts.toString() );
    }
}
