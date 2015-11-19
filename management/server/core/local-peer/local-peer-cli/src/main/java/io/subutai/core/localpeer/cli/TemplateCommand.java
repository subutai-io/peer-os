package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.protocol.Template;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "lp", name = "template" )
public class TemplateCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;
    @Argument( index = 0, name = "templateName", multiValued = false, description = "Template name" )
    private String templateName;


    public TemplateCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Template templateInfo = localPeer.getTemplate( templateName );
        System.out.println( "Template Info: " + templateInfo );
        return null;
    }
}
