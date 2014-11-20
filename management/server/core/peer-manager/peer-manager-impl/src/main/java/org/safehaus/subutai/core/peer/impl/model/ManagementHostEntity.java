package org.safehaus.subutai.core.peer.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.SubutaiInitException;
import org.safehaus.subutai.core.peer.impl.Commands;


@Entity
@DiscriminatorValue( "M" )
@Access( AccessType.FIELD )
public class ManagementHostEntity extends AbstractSubutaiHost implements ManagementHost
{
    @Column
    String name = "Subutai Management Host";


    private ManagementHostEntity()
    {
    }


    //    public ManagementHostEntity( final String id, final String peerId, final String hostname )
    //    {
    //        this.id = id;
    //        this.peerId = peerId;
    //        this.hostname = hostname;
    //    }
    //


    public ManagementHostEntity( final String peerId, final ResourceHostInfo resourceHostInfo )
    {
        super( peerId, resourceHostInfo );
    }


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


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
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


    public void addAptSource( final String hostname, final String ip ) throws PeerException
    {
        RequestBuilder rb = new RequestBuilder( Commands.getAddAptSourceCommand( hostname, ip ) );
        rb.withCwd( "/etc/apt-cacher/" );
        try
        {
            CommandResult commandResult = execute( rb );
            if ( !commandResult.hasCompleted() )
            {
                throw new CommandException( "Command execution failed." );
            }
        }
        catch ( CommandException e )
        {
            throw new PeerException( "Could not add remote host as apt source", e.toString() );
        }
    }


    public void removeAptSource( final String host, final String ip ) throws PeerException
    {
        RequestBuilder rb = new RequestBuilder( Commands.getRemoveAptSourceCommand( host, ip ) );
        rb.withCwd( "/etc/apt-cacher/" );
        try
        {
            CommandResult commandResult = execute( rb );
            if ( !commandResult.hasCompleted() )
            {
                throw new CommandException( "Command execution failed." );
            }
        }
        catch ( CommandException e )
        {
            throw new PeerException( "Could not add remote host as apt source", e.toString() );
        }
    }
}
