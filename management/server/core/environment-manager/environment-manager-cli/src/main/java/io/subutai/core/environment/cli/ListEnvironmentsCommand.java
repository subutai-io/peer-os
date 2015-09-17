package io.subutai.core.environment.cli;


import java.util.Date;

import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.PeerConf;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


/**
 * List all existing environments
 */
@Command( scope = "environment", name = "list", description = "Command to view environment" )
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

            for ( PeerConf peerConf : environment.getPeerConfs() )
            {
                System.out.println( String.format( "\t%s\t%s\t%s\t%s", peerConf.getN2NConfig().getPeerId(),
                        peerConf.getN2NConfig().getAddress(), peerConf.getN2NConfig().getInterfaceName(),
                        peerConf.getN2NConfig().getCommunityName() ) );
            }

            System.out.println( "-----------------------------------------------------------------" );
        }

        return null;
    }
}
