package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.Quota;


/**
 * View target environment brief info
 */
@Command( scope = "environment", name = "view", description = "Command to view environment" )
public class ViewEnvironmentCommand extends SubutaiShellCommandSupport
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


    public ViewEnvironmentCommand( final EnvironmentManager environmentManager, final PeerManager peerManager )
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


            if ( peerStatus == RegistrationStatus.APPROVED )
            {
                System.out.println( "Container state: " + containerHost.getState() );

                try
                {
                    final ContainerQuota quota = containerHost.getQuota();

                    System.out.println( "Granted resources: " );
                    System.out.println( "Type\tValue\tThreshold" );
                    for ( Quota q : quota.getAll() )
                    {
                        System.out.println( String.format( "%s\t%s\t%s", q.getResource().getContainerResourceType(),
                                q.getResource().getPrintValue(), q.getThreshold() ) );
                    }
                }
                catch ( Exception e )
                {
                    System.out.println( "ERROR: " + e.getMessage() );
                }
            }
        }

        return null;
    }
}
