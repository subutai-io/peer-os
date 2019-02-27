package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.Template;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "clone-container" )
public class CloneContainerCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "resource host", description = "Resource host id", required = true )
    private String rhId;

    @Argument( index = 1, name = "template name", description = "Name of template", required = true )
    private String templateName;

    @Argument( index = 2, name = "container name", description = "Name of new container", required = true )
    private String containerName;

    @Argument( index = 3, name = "ip", description = "IP", required = true )
    private String ip;

    @Argument( index = 4, name = "vlan", description = "VLAN", required = true )
    private int vlan;

    @Argument( index = 5, name = "env id", description = "Environment id", required = true )
    private String envId;

    @Argument( index = 6, name = "backup file", description = "Full path to backup file" )
    private String backupFile;

    private final LocalPeer localPeer;


    public CloneContainerCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {


        ResourceHost resourceHost = localPeer.getResourceHostById( rhId );
        Template template = localPeer.getTemplateByName( templateName );

        String contId =
                resourceHost.cloneContainer( template, containerName, containerName, ip, vlan, envId, backupFile );
        System.out.println( contId );

        return null;
    }
}
