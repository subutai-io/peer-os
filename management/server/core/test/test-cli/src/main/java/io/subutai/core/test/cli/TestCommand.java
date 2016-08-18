package io.subutai.core.test.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Command;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.PeerTemplatesDownloadProgress;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "test", name = "do", description = "test command" )
public class TestCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( TestCommand.class.getName() );


    @Override
    protected Object doExecute()
    {

        try
        {
            EnvironmentManager environmentManager = ServiceLocator.getServiceNoCache( EnvironmentManager.class );

            for ( Environment environment : environmentManager.getEnvironments() )
            {
                for ( Peer peer : environment.getPeers() )
                {
                    PeerTemplatesDownloadProgress downloadProgress =
                            peer.getTemplateDownloadProgress( environment.getEnvironmentId() );

                    System.out.println( downloadProgress );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return null;
    }
}
