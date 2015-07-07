package io.subutai.wol.impl;


import java.util.ArrayList;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.PeerException;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.wol.api.WolManager;
import io.subutai.wol.api.WolManagerException;

import com.google.common.base.Preconditions;


public class WolImpl implements WolManager
{
    private final PeerManager peerManager;
    protected Commands commands = new Commands();


    //Default Impl Constructor
    public WolImpl( final PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager );

        this.peerManager = peerManager;
    }


    @Override
    public CommandResult sendMagicPackageByMacId( String macID ) throws WolManagerException
    {
        return execute( getManagementHost(), commands.getSendWakeOnLanCommand( macID ) );
    }


    @Override
    public Boolean sendMagicPackageByList( ArrayList<String> macList ) throws WolManagerException
    {

        for ( String aMacList : macList )
        {
            CommandResult commandResult = execute( getManagementHost(), commands.getSendWakeOnLanCommand( aMacList ) );
            if ( !commandResult.hasSucceeded() )
            {
                return false;
            }
        }
        return true;
    }


    //Returning Command Result for single Wake on lan execution
    private CommandResult execute( Host host, RequestBuilder requestBuilder ) throws WolManagerException
    {
        try
        {
            CommandResult result = host.execute( requestBuilder );
            if ( !result.hasSucceeded() )
            {
                throw new WolManagerException(
                        String.format( "Command failed: %s, %s", result.getStdErr(), result.getStatus() ) );
            }
            else
            {
                return result;
            }
        }
        catch ( CommandException e )
        {
            throw new WolManagerException( e );
        }
    }


    //getting Management Host
    private ManagementHost getManagementHost() throws WolManagerException
    {
        try
        {
            return peerManager.getLocalPeer().getManagementHost();
        }
        catch ( PeerException e )
        {
            throw new WolManagerException( e );
        }
    }
}
