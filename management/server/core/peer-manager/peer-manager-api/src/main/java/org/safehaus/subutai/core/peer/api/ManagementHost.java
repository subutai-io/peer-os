package org.safehaus.subutai.core.peer.api;


import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.RequestBuilder;

import com.google.common.collect.Sets;


/**
 * Management host implementation.
 */
public class ManagementHost extends SubutaiHost
{
    //    private static final String DEFAULT_MANAGEMENT_HOSTNAME = "management";
    private Set<ResourceHost> resourceHosts = Sets.newHashSet();


    public ManagementHost( final Agent agent, UUID peerId )
    {
        super( agent, peerId );
    }

    //
    //    @Override
    //    public boolean isConnected( final Host host )
    //    {
    //        //TODO: Implement resource host check
    //        return true;
    //    }


    public Set<ResourceHost> getResourceHosts()
    {
        return resourceHosts;
    }


    public void setResourceHosts( final Set<ResourceHost> resourceHosts )
    {
        this.resourceHosts = resourceHosts;
    }


    public ResourceHost getResourceHostByName( final String hostname )
    {
        return findResourceHostByName( hostname );
    }


    public void addResourceHost( final ResourceHost host )
    {
        if ( host == null )
        {
            throw new IllegalArgumentException( "Resource host could not be null." );
        }
        resourceHosts.add( host );
    }


    private ResourceHost findResourceHostByName( final String hostname )
    {
        ResourceHost result = null;
        Iterator iterator = resourceHosts.iterator();

        while ( result == null && iterator.hasNext() )
        {
            ResourceHost host = ( ResourceHost ) iterator.next();

            if ( host.getHostname().equals( hostname ) )
            {
                result = host;
            }
        }
        return result;
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
                "\\}-[a-f0-9]\\{12\\}\\s*http:\\/\\/%s/ksks\\s*,,g'' apt-cacher.conf > apt-cacher.conf"
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
