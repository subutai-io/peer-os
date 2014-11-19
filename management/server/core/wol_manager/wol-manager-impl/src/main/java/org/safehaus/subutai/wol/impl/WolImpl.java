package org.safehaus.subutai.wol.impl;

import com.google.common.base.Preconditions;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.peer.api.*;
import org.safehaus.subutai.wol.api.WolManager;
import org.safehaus.subutai.wol.api.WolManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;


/**
 * Created by emin on 14/11/14.
 */
public class WolImpl implements WolManager {
    private static final Logger LOG = LoggerFactory.getLogger(WolImpl.class.getName());
    private final PeerManager peerManager;
    protected Commands commands = new Commands();


    //Default Impl Constructor
    public WolImpl( final PeerManager peerManager )
    {
        Preconditions.checkNotNull(peerManager);

        this.peerManager = peerManager;
    }


    @Override
    public CommandResult sendMagicPackagebyMacId(String macID) throws WolManagerException
    {
        return execute( getManagementHost(), commands.getSendWakeOnLanCommand( macID ) );
    }


    @Override
    public Boolean sendMagicPackagebyList(ArrayList<String> macList)  throws WolManagerException
    {

        for(int i=0 ; i<macList.size() ; i++ )
        {
            CommandResult commandResult = execute( getManagementHost(), commands.getSendWakeOnLanCommand(macList.get(i)));
            if ( ! commandResult.hasSucceeded() )
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
