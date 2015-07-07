package io.subutai.core.key.cli;


import org.safehaus.subutai.common.peer.Host;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.key.api.KeyInfo;
import io.subutai.core.key.api.KeyManager;
import io.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;


@Command( scope = "km", name = "gen" )
public class GenerateCommand extends SubutaiShellCommandSupport
{
    private KeyManager keyManager;
    private PeerManager peerManager;

    @Argument( index = 0, name = "name", multiValued = false, required = true, description = "Real name of key owner" )
    private String name;

    @Argument( index = 1, name = "email", multiValued = false, required = true, description = "E-mail address of key "
            + "owner" )
    private String email;


    public GenerateCommand( final KeyManager keyManager, final PeerManager peerManager )
    {
        this.keyManager = keyManager;
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Host managementHost = peerManager.getLocalPeer().getManagementHost();
        KeyInfo keyInfo = keyManager.generateKey( managementHost, name, email );

        System.out.println(
                String.format( "Key successfully generated: %s %s %s", keyInfo.getRealName(), keyInfo.getEmail(),
                        keyInfo.getPublicKeyId() ) );
        return null;
    }
}
