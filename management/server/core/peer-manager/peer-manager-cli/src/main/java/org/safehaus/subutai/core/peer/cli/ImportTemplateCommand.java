package org.safehaus.subutai.core.peer.cli;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;


/**
 *
 */
@Command( scope = "peer", name = "import-template" )
public class ImportTemplateCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;

    //    private PeerCommandDispatcher peerCommandDispatcher;

    private TemplateRegistry templateRegistry;

    @Argument( index = 0, name = "peerId", multiValued = false, description = "Remote Peer UUID" )
    private String peerId;

    @Argument( index = 1, name = "templateName", multiValued = false, description = "Remote template name" )
    private String templateName;


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }

    //
    //    public void setPeerCommandDispatcher( final PeerCommandDispatcher peerCommandDispatcher )
    //    {
    //        this.peerCommandDispatcher = peerCommandDispatcher;
    //    }


    public void setTemplateRegistry( final TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        //System.out.println( "Start..." );
        Template template = templateRegistry.getTemplate( templateName );
        if ( template != null )
        {
            System.out.println( "Template already registered." );
            return -1;
        }

        //        peerManager.peers();

        template = peerManager.getPeer( UUID.fromString( peerId ) ).getTemplate( templateName );
        if ( template != null )
        {
            System.out.println( "Template successfully obtained: " + template );
        }
        else
        {
            System.out.println( String.format( "Could not obtain template %s from %s: ", templateName, peerId ) );
        }


        System.out.println( "Registering template : " + template );
        boolean result = templateRegistry.registerTemplate( template );
        if ( result )
        {
            System.out.println( "Template registered." );
        }
        else
        {
            System.out.println( "Template registration failed." );
        }
        return null;
    }
}
