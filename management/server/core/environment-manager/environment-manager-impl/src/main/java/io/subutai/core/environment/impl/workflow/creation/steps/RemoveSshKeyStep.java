package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


//todo move logic to Peer
public class RemoveSshKeyStep
{
    private final String sshKey;
    private final EnvironmentImpl environment;
    private final TrackerOperation trackerOperation;
    protected CommandUtil commandUtil = new CommandUtil();


    public RemoveSshKeyStep( final String sshKey, final EnvironmentImpl environment,
                             final TrackerOperation trackerOperation )
    {
        this.sshKey = sshKey;
        this.environment = environment;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentManagerException
    {

        if ( !Strings.isNullOrEmpty( sshKey ) )
        {
            Set<Host> hosts = Sets.newHashSet();
            hosts.addAll( environment.getContainerHosts() );

            Map<Host, CommandResult> results =
                    commandUtil.executeParallelSilent( getRemoveSshKeyCommand( sshKey ), hosts );

            for ( Host succeededHost : results.keySet() )
            {
                trackerOperation.addLog( String.format( "SSH key removed on host %s", succeededHost.getHostname() ) );
            }

            hosts.removeAll( results.keySet() );

            for ( Host failedHost : hosts )
            {
                trackerOperation
                        .addLog( String.format( "SSH key removal failed on host %s", failedHost.getHostname() ) );
            }

            if ( !hosts.isEmpty() )
            {
                throw new EnvironmentManagerException( "Failed to remove SSH key on all hosts" );
            }

            environment.removeSshKey( sshKey );
        }
    }


    public RequestBuilder getRemoveSshKeyCommand( final String key )
    {
        return new RequestBuilder( String.format( "chmod 700 %1$s && " +
                "sed -i \"\\,%3$s,d\" %2$s && " +
                "chmod 644 %2$s", Common.CONTAINER_SSH_FOLDER, Common.CONTAINER_SSH_FILE, key ) );
    }
}
