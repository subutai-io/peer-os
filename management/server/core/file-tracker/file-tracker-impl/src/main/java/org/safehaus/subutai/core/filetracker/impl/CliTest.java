package org.safehaus.subutai.core.filetracker.impl;


import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.filetracker.api.FileTracker;
import org.safehaus.subutai.core.filetracker.api.FileTrackerException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Needed mostly for testing FileTracker
 */
@Command( scope = "file-tracker", name = "test" )
public class CliTest extends OsgiCommandSupport implements ResponseListener
{

    private static final String[] CONFIG_POINTS = new String[] { "/etc", "/etc/subutai-agent" };

    private PeerManager peerManager;

    private FileTracker fileTracker;


    public void setPeerManager( final PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager, "PeerManager is null." );
        this.peerManager = peerManager;
    }


    public void setFileTracker( FileTracker fileTracker )
    {
        Preconditions.checkNotNull( fileTracker, "FileTracker is null." );
        this.fileTracker = fileTracker;
    }


    protected Object doExecute() throws PeerException, FileTrackerException
    {

        fileTracker.addListener( this );

        LocalPeer localPeer = peerManager.getLocalPeer();

        ManagementHost managementHost = localPeer.getManagementHost();

        fileTracker.createConfigPoints( managementHost, CONFIG_POINTS );

        return null;
    }


    @Override
    public void onResponse( Response response )
    {
        //some dummy method
    }
}
