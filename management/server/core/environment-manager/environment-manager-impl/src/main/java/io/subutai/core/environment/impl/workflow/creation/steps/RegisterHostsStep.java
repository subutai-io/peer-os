package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class RegisterHostsStep
{
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
        configureHosts( environment.getContainerHosts() );
    }


    public void configureHosts( final Set<EnvironmentContainerHost> containerHosts ) throws EnvironmentManagerException
    {
        Map<Integer, Set<EnvironmentContainerHost>> hostGroups = Maps.newHashMap();

        //group containers by host group
        for ( EnvironmentContainerHost containerHost : containerHosts )
        {
            int hostGroupId = ( ( EnvironmentContainerImpl ) containerHost ).getHostsGroupId();
            Set<EnvironmentContainerHost> groupedContainers = hostGroups.get( hostGroupId );

            if ( groupedContainers == null )
            {
                groupedContainers = Sets.newHashSet();
                hostGroups.put( hostGroupId, groupedContainers );
            }

            groupedContainers.add( containerHost );
        }

        //configure hosts on each group
        for ( Map.Entry<Integer, Set<EnvironmentContainerHost>> hostGroup : hostGroups.entrySet() )
        {
            int hostGroupId = hostGroup.getKey();
            Set<EnvironmentContainerHost> groupedContainers = hostGroup.getValue();

            //ignore group ids <= 0
            if ( hostGroupId > 0 )
            {
                Set<Host> hosts = Sets.newHashSet();
                hosts.addAll( groupedContainers );

                //assume that inside one host group the domain name must be the same for all containers
                //so pick one container's domain name as the group domain name

                String localDomain =
                        ( ( EnvironmentContainerImpl ) groupedContainers.iterator().next() ).getDomainName();

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
                }

                hosts.removeAll( succeededHosts );

                for ( Host failedHost : hosts )
                {
                    trackerOperation
                            .addLog( String.format( "Host registration failed on host %s", failedHost.getHostname() ) );
                }

                if ( !hosts.isEmpty() )
                {
                    throw new EnvironmentManagerException( "Failed to register all hosts" );
                }
            }
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
