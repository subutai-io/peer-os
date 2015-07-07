package io.subutai.core.key.cli;


import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.common.peer.Host;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.key.api.KeyInfo;
import io.subutai.core.key.api.KeyManager;
import io.subutai.core.key.api.KeyManagerException;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;


@Command( scope = "km", name = "list" )
public class ListCommand extends SubutaiShellCommandSupport
{
    private KeyManager keyManager;
    private PeerManager peerManager;

    @Argument( index = 0, name = "key ID", multiValued = false, required = false, description = "Key ID" )
    private String keyId;


    public ListCommand( final KeyManager keyManager, final PeerManager peerManager )
    {
        this.keyManager = keyManager;
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Host managementHost = peerManager.getLocalPeer().getManagementHost();
        if ( keyId != null )
        {
            listKey( managementHost );
        }
        else
        {
            listAllKeys( managementHost );
        }
        return null;
    }


    private void listKey( Host managementHost ) throws KeyManagerException
    {
        KeyInfo keyInfo = keyManager.getKey( managementHost, keyId );

        System.out.println(
                String.format( "%s %s %s", keyInfo.getRealName(), keyInfo.getEmail(), keyInfo.getPublicKeyId() ) );
    }


    private void listAllKeys( Host managementHost ) throws KeyManagerException
    {
        Set<KeyInfo> keys = keyManager.getKeys( managementHost );

        for ( Iterator<KeyInfo> iterator = keys.iterator(); iterator.hasNext(); )
        {
            KeyInfo keyInfo = iterator.next();
            System.out.println(
                    String.format( "%s %s %s", keyInfo.getRealName(), keyInfo.getEmail(), keyInfo.getPublicKeyId() ) );
        }
    }
}
