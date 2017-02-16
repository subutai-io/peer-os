package io.subutai.core.test.cli;


import java.util.Set;

import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "test", name = "do", description = "test command" )
public class TestCommand extends SubutaiShellCommandSupport
{


    @Override
    protected Object doExecute() throws Exception
    {
        LocalPeer localPeer = ServiceLocator.lookup( LocalPeer.class );

        Set<String> names = localPeer.getResourceHosts().iterator().next().listExistingContainerNames();

        for ( String name : names )
        {
            System.out.println( name );
        }

        return null;
    }
}
