package org.safehaus.subutai.core.key.cli;


import org.safehaus.subutai.common.peer.Host;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.key.api.KeyInfo;
import org.safehaus.subutai.core.key.api.KeyManager;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;


@Command( scope = "km", name = "gen-subkey" )
public class GenerateSubKeyCommand extends SubutaiShellCommandSupport
{
    private KeyManager keyManager;
    private PeerManager peerManager;

    @Argument( index = 0, name = "key ID", multiValued = false, required = true, description = "Real name of key "
            + "owner" )
    private String keyId;


    public GenerateSubKeyCommand( final KeyManager keyManager, final PeerManager peerManager )
    {
        this.keyManager = keyManager;
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Host managementHost = peerManager.getLocalPeer().getManagementHost();
        KeyInfo keyInfo = keyManager.getKey( managementHost, keyId );
        String subKeyId = keyManager.generateSubKey( managementHost, keyId );

        System.out.println(
                String.format( "Sub Key %s successfully generated for: %s %s %s", subKeyId, keyInfo.getRealName(),
                        keyInfo.getEmail(), keyInfo.getPublicKeyId() ) );
        return null;
    }
}
