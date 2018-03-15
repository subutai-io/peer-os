package io.subutai.core.peer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.PeerException;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "set-public-url", description = "Sets public URL of registered peer" )
public class SetPublicUrlCommand extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "peer id", required = true, multiValued = false, description = "peer identifier" )
    private String peerId;

    @Argument( index = 1, name = "publicUrl", required = true, multiValued = false, description = "New public URL" )
    private String publicUrl;

    @Argument( index = 2, name = "securePort", required = true, multiValued = false, description = "Secure port. "
            + "Default: 8443" )
    private String securePort;

    private PeerManager peerManager;


    public SetPublicUrlCommand( PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public void setPublicUrl( final String publicUrl )
    {
        this.publicUrl = publicUrl;
    }


    public void setSecurePort( final String securePort )
    {
        this.securePort = securePort;
    }


    @Override
    protected Object doExecute() throws PeerException
    {
        peerManager.setPublicUrl( peerId, publicUrl, Integer.parseInt( securePort ), false );
        System.out.println( "Public URL successfully updated." );
        return null;
    }
}
