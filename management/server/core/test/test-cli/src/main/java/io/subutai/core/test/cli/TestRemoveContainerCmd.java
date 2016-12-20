package io.subutai.core.test.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "test", name = "remove-container", description = "test command" )
public class TestRemoveContainerCmd extends SubutaiShellCommandSupport
{

    @Argument( name = "containerId", description = "Container id" )
    protected String containerId;


    @Override
    protected Object doExecute() throws HostNotFoundException
    {
        ResourceHost resourceHost =
                ServiceLocator.lookup( LocalPeer.class ).getResourceHostByContainerId( containerId );

        resourceHost.removeContainerHost( resourceHost.getContainerHostById( containerId ) );

        return null;
    }
}
