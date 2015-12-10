package io.subutai.core.peer.cli;


import java.io.StringWriter;

import org.codehaus.jackson.map.ObjectMapper;

import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "policy" )
public class PeerPolicyCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;
    private ObjectMapper objectMapper = new ObjectMapper();


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        PeerPolicy policy = peerManager.getAvailablePolicy();
        StringWriter w = new StringWriter();
        w.append( "Available peer policy:\n" );
        objectMapper.writerWithDefaultPrettyPrinter().writeValue( w, policy );
        w.append( "\nList of granted policies:\n" );

        for ( Peer peer : peerManager.getPeers() )
        {
            final PeerPolicy grantedPolicy = peerManager.getPolicy( peer.getId() );
            if ( grantedPolicy != null )
            {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue( w, grantedPolicy );
                w.append( "\n" );
            }
            else
            {
                w.append( "Nothing granted to: " + peer.getId() + "\n" );
            }
        }

        System.out.println( w.toString() );
        return null;
    }
}
