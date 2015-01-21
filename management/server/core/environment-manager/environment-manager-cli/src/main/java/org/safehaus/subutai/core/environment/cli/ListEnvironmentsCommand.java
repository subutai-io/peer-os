package org.safehaus.subutai.core.environment.cli;


import java.util.List;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 6/21/14.
 */
@Command(scope = "environment", name = "ls", description = "Command to list environments",
        detailedDescription = "Command to list environments")
public class ListEnvironmentsCommand extends OsgiCommandSupport
{

    EnvironmentManager environmentManager;


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute()
    {
        List<Environment> environments = environmentManager.getEnvironments();
        if ( environments != null )
        {
            if ( !environments.isEmpty() )
            {
                for ( Environment environment : environments )
                {
                    System.out.println( String.format( "%s %s", environment.getName(), environment.getId() ) );
                    for ( ContainerHost containerHost : environment.getContainerHosts() )
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append( containerHost.getHostname() );
                        Peer peer = null;
                        try
                        {
                            peer = containerHost.getPeer();
                            sb.append( " " + peer.getPeerInfo().getIp() );
                            sb.append( " " + containerHost.getTemplate().getTemplateName() );
                            sb.append( " " + ( containerHost.isConnected() ? "CONNECTED" : "DISCONNECTED" ) );
                        }
                        catch ( PeerException e )
                        {
                            sb.append( e.toString() );
                        }

                        System.out.println( String.format( "\t%s", sb.toString() ) );
                    }
                }
            }
            else
            {
                System.out.println( "No environments found." );
            }
        }
        else
        {
            System.out.println( "No environments found." );
        }
        return null;
    }
}
