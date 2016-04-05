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
public class AddSshKeyStep
{
    private final String sshKey;
    private final EnvironmentImpl environment;
    private final TrackerOperation trackerOperation;
    protected CommandUtil commandUtil = new CommandUtil();


    public AddSshKeyStep( final String sshKey, final EnvironmentImpl environment,
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
                    commandUtil.executeParallelSilent( getAppendSshKeyCommand( sshKey ), hosts );

            for ( Host succeededHost : results.keySet() )
            {
                trackerOperation.addLog( String.format( "SSH key added to host %s", succeededHost.getHostname() ) );
            }

            hosts.removeAll( results.keySet() );

            for ( Host failedHost : hosts )
            {
                trackerOperation
                        .addLog( String.format( "SSH key addition failed on host %s", failedHost.getHostname() ) );
            }

            if ( !hosts.isEmpty() )
            {
                throw new EnvironmentManagerException( "Failed to add SSH key to all hosts" );
            }

            environment.addSshKey( sshKey );
        }
    }


    public RequestBuilder getAppendSshKeyCommand( String key )
    {
        return new RequestBuilder( String.format(
                "mkdir -p '%1$s' && " + "echo '%3$s' >> '%2$s' && " + "chmod 700 -R '%1$s' && "
                        + "sort -u '%2$s' -o '%2$s'", Common.CONTAINER_SSH_FOLDER, Common.CONTAINER_SSH_FILE, key ) );
    }
}
