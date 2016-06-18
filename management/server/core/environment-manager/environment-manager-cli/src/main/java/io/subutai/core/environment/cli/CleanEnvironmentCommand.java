package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.quota.Quota;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


/**
 * Clean target environment
 */
@Command( scope = "environment", name = "clean", description = "Command to view environment" )
public class CleanEnvironmentCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "envId", description = "Environment id",
            index = 0, multiValued = false, required = true )
    /**
     * {@value environmentId} environment id to view info about
     * <p>{@code required = true}</p>
     */
            String environmentId;

    private final EnvironmentManager environmentManager;
    private final PeerManager peerManager;


    public CleanEnvironmentCommand( final EnvironmentManager environmentManager, final PeerManager peerManager )
    {
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( peerManager );

        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        Environment environment = environmentManager.loadEnvironment( environmentId );


        System.out.println( String.format( "Environment name %s", environment.getName() ) );

        for ( EnvironmentContainerHost containerHost : environment.getContainerHosts() )
        {
            RegistrationStatus peerStatus = peerManager.getRemoteRegistrationStatus( containerHost.getPeerId() );
            System.out.println( "-----------------------------------------------------------------" );

            System.out.println( String.format( "Container id: %s", containerHost.getId() ) );
            System.out.println( String.format( "Container hostname: %s", containerHost.getHostname() ) );
            System.out.println( String.format( "Environment id: %s", containerHost.getEnvironmentId() ) );
            System.out.println( String.format( "Peer id: %s", containerHost.getPeerId() ) );
            System.out.println( String.format( "Peer status: %s", peerStatus ) );
            System.out.println( String.format( "Template name: %s", containerHost.getTemplateName() ) );
            System.out.println( String.format( "IP: %s",
                    containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp() ) );


            if ( peerStatus == RegistrationStatus.NOT_REGISTERED )
            {
                environment.destroyContainer( containerHost, false );
                System.out.println( "Container destroyed: " + containerHost.getHostname() );
            }
        }

        return null;
    }
}
