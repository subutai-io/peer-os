package org.safehaus.subutai.core.peer.cli;


import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.util.SecurityUtil;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.apache.shiro.subject.Subject;


@Command( scope = "peer", name = "hosts" )
public class HostsCommand extends OsgiCommandSupport
{
    private static Logger log = LoggerFactory.getLogger( HostsCommand.class );
    DateFormat fmt = new SimpleDateFormat( "dd.MM.yy HH:mm:ss.SS" );
    private PeerManager peerManager;
    private IdentityManager identityManager;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    //    public void setSecurityManager( final SecurityManager securityManager )
    //    {
    //        this.securityManager = securityManager;
    //    }


    @Override
    protected Object doExecute() throws Exception
    {
        Serializable sessionId = SecurityUtil.getSessionId();

        log.debug( String.format( "Session ID: %s", sessionId ) );

        Subject subject = identityManager.getSubject( sessionId );

        log.debug( String.format( "Subject: %s", subject ) );

        identityManager.getUser( subject.getPrincipal().toString() );

        LocalPeer localPeer = peerManager.getLocalPeer();
        //        localPeer.init();
        ManagementHost managementHost = localPeer.getManagementHost();
        if ( managementHost == null )
        {
            System.out.println( "Management host not available." );
            return null;
        }

        System.out.println( "Time:" + fmt.format( System.currentTimeMillis() ) );
        System.out.println( "List of hosts in local peer:" );
        print( managementHost, "" );
        for ( ResourceHost resourceHost : localPeer.getResourceHosts() )
        {
            print( resourceHost, "\t" );
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                print( containerHost, "\t\t" );
            }
        }
        return null;
    }

    //
    //    private String getSessionId( final Set<Principal> principals )
    //    {
    //        String result = "";
    //        for ( Principal p : principals )
    //        {
    //            if ( p.getName().contains( "session" ) )
    //            {
    //                result = p.getName().split( ":" )[1];
    //            }
    //        }
    //        return result;
    //    }


    private void print( Host host, String padding ) throws PeerException
    {
        String lastHeartbeat = fmt.format( host.getLastHeartbeat() );
        String containerInfo =
                String.format( "%s\t(%s) ", host.isConnected() ? " CONNECTED" : " DISCONNECTED", lastHeartbeat );
        if ( host instanceof ContainerHost )
        {
            ContainerHost c = ( ContainerHost ) host;
            containerInfo += c.getState();
            //            if ( c.getEnvironmentId() != null )
            //            {
            //                containerInfo += " " + c.getEnvironmentId();
            //            }
            //
            //            if ( c.getInitiatorPeerId() != null )
            //            {
            //                Peer peer = peerManager.getPeer( UUID.fromString( c.getPeerId() ) );
            //                if ( peer != null )
            //                {
            //                    containerInfo += " " + peer.getPeerInfo().getIp();
            //                }
            //            }
        }

        System.out
                .println( String.format( "%s+--%s %s %s", padding, host.getHostname(), host.getId(), containerInfo ) );
    }
}
