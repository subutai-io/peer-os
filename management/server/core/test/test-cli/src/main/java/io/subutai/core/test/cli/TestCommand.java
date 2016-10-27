package io.subutai.core.test.cli;


import java.util.Map;

import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.PeerTemplatesDownloadProgress;
import io.subutai.common.environment.RhTemplatesDownloadProgress;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "test", name = "do", description = "test command" )
public class TestCommand extends SubutaiShellCommandSupport
{


    @Override
    protected Object doExecute()
    {

        try
        {
            EnvironmentManager environmentManager = ServiceLocator.getServiceNoCache( EnvironmentManager.class );

            Preconditions.checkNotNull( environmentManager );

            for ( Environment environment : environmentManager.getEnvironments() )
            {
                System.out.format( "Environment \"%s\":%n", environment.getName() );

                for ( Peer peer : environment.getPeers() )
                {
                    PeerTemplatesDownloadProgress downloadProgress =
                            peer.getTemplateDownloadProgress( environment.getEnvironmentId() );

                    System.out.format( "\tPeer \"%s\":%n", peer.getName() );

                    for ( RhTemplatesDownloadProgress rhProgress : downloadProgress.getTemplatesDownloadProgresses() )
                    {
                        System.out.format( "\t\tRH \"%s\":%n", rhProgress.getRhId() );

                        for ( Map.Entry<String, Integer> templateProgress : rhProgress.getTemplatesDownloadProgresses()
                                                                                      .entrySet() )
                        {
                            System.out.format( "\t\t\tTemplate \"%s\" -> %d%% downloaded%n", templateProgress.getKey(),
                                    templateProgress.getValue() );
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            log.warn( e.getMessage() );
        }
        return null;
    }
}
