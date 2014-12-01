package org.safehaus.subutai.wol.impl;


import com.google.common.base.Preconditions;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.wol.api.WolManager;
import org.safehaus.subutai.wol.api.WolManagerException;

import java.util.ArrayList;


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
