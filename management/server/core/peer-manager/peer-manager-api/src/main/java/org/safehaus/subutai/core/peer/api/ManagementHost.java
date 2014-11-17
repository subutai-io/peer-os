package org.safehaus.subutai.core.peer.api;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;


/**
 * Management host implementation.
 */
public class ManagementHost extends SubutaiHost
{
    //    private static final String DEFAULT_MANAGEMENT_HOSTNAME = "management";
    //    private Set<ResourceHost> resourceHosts = Sets.newHashSet();


    public ManagementHost( final Agent agent, UUID peerId )
    {
        super( agent, peerId );
    }


//    public ManagementHost( ResourceHostInfo resourceHostInfo )
//    {
//        super( resourceHostInfo );
//    }


    public void init() throws SubutaiInitException
    {
        //        try
        //        {
        //            createFlows();
        //        }
        //        catch ( CommandException e )
        //        {
        //            throw new SubutaiInitException( "Could not create network flows." );
        //        }
    }


    private void createFlows() throws CommandException
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "subutai management_network -b br-int 1 normal normal && " );
        sb.append( "subutai management_network -b br-tun 1 normal normal && " );
        sb.append( "subutai management_network -b br - tun 2500 arp drop 10.10 .10 .0 / 24 10.10 .10 .0 / 24 1 && " );
        sb.append( "subutai management_network -b br - tun 2500 icmp drop 10.10 .10 .0 / 24 10.10 .10 .0 / 24 1" );
        CommandResult commandResult = execute( new RequestBuilder( sb.toString() ) );
        if ( !commandResult.hasSucceeded() )
        {
            throw new CommandException( "Flow commands fail." );
        }
    }


    public void addAptSource( final String host, final String ip ) throws PeerException
    {
        String cmd = String.format( "sed '/^path_map.*$/ s/$/ ; %s %s/' apt-cacher.conf > apt-cacher.conf"
                        + ".new && mv apt-cacher.conf.new apt-cacher.conf && /etc/init.d/apt-cacher reload", host,
                ( "http://" + ip + "/ksks" ).replace( ".", "\\." ).replace( "/", "\\/" ) );

        RequestBuilder rb = new RequestBuilder( cmd );
        rb.withCwd( "/etc/apt-cacher/" );
        try
        {
            execute( rb );
        }
        catch ( CommandException e )
        {
            throw new PeerException( "Could not add remote host as apt source", e.toString() );
        }
    }


    public void removeAptSource( final String host, final String ip ) throws PeerException
    {
        String cmd = String.format( "sed -e 's,;\\s*[a-f0-9]\\{8\\}-[a-f0-9]\\{4\\}-[a-f0-9]\\{4\\}-[a-f0-9]\\{4" +
                        "\\}-[a-f0-9]\\{12\\}\\s*http:\\/\\/%s/ksks\\s*,,g' apt-cacher.conf > apt-cacher.conf"
                        + ".new && mv apt-cacher.conf.new apt-cacher.conf && /etc/init.d/apt-cacher reload",
                ip.replace( ".", "\\." ) );

        RequestBuilder rb = new RequestBuilder( cmd );
        rb.withCwd( "/etc/apt-cacher/" );
        try
        {
            execute( rb );
        }
        catch ( CommandException e )
        {
            throw new PeerException( "Could not add remote host as apt source", e.toString() );
        }
    }
}
