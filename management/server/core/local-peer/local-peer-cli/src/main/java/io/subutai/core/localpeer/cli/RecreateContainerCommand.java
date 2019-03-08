package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "recreate-container" )
public class RecreateContainerCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "resource host", description = "Resource host id", required = true )
    private String rhId;

    @Argument( index = 1, name = "hostname", description = "Hostname of new container", required = true )
    private String hostname;

    @Argument( index = 2, name = "container name", description = "Name of new container", required = true )
    private String containerName;

    @Argument( index = 3, name = "ip", description = "IP", required = true )
    private String ip;

    @Argument( index = 4, name = "vlan", description = "VLAN", required = true )
    private int vlan;

    @Argument( index = 5, name = "env id", description = "Environment id", required = true )
    private String envId;

    private final LocalPeer localPeer;


    public RecreateContainerCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {


        ResourceHost resourceHost = localPeer.getResourceHostById( rhId );

        String contId = resourceHost.recreateContainer( containerName, hostname, ip, vlan, envId );
        System.out.println( contId );

        return null;
    }
}
