package io.subutai.core.env.cli;


import java.util.Date;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentPeer;
import io.subutai.core.env.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;

import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;


/**
 * List all existing environments
 */
@Command( scope = "env", name = "list", description = "Command to view environment" )
public class ListEnvironmentsCommand extends SubutaiShellCommandSupport
{

    private final EnvironmentManager environmentManager;


    public ListEnvironmentsCommand( final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( environmentManager );

        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        for ( Environment environment : environmentManager.getEnvironments() )
        {
            System.out.println( String.format( "Environment id %s", environment.getId() ) );
            System.out.println( String.format( "Environment name %s", environment.getName() ) );
            System.out.println(
                    String.format( "Environment creation time %s", new Date( environment.getCreationTimestamp() ) ) );
            System.out.println( String.format( "Environment status %s", environment.getStatus() ) );
            System.out.println( String.format( "Subnet CIDR %s", environment.getSubnetCidr() ) );
            System.out.println( String.format( "N2N config:", environment.getSubnetCidr() ) );

            for ( EnvironmentPeer environmentPeer : environment.getEnvironmentPeers() )
            {
                System.out.println( String.format( "\t%s\t%s", environmentPeer.getPeerId(), environmentPeer.getIp() ) );
            }

            System.out.println( "-----------------------------------------------------------------" );
        }

        return null;
    }
}
