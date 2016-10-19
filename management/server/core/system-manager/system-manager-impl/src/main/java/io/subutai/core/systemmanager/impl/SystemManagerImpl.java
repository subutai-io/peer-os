package io.subutai.core.systemmanager.impl;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.settings.SettingsListener;
import io.subutai.common.settings.SubutaiInfo;
import io.subutai.common.settings.SystemSettings;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;
import io.subutai.core.systemmanager.impl.pojo.AdvancedSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.NetworkSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.PeerSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.SystemInfoPojo;


public class SystemManagerImpl implements SystemManager
{
    private static final Logger LOG = LoggerFactory.getLogger( SystemManagerImpl.class );

    private IdentityManager identityManager;
    private PeerManager peerManager;

    protected Set<SettingsListener> listeners =
            Collections.newSetFromMap( new ConcurrentHashMap<SettingsListener, Boolean>() );

    protected ExecutorService notifierPool = Executors.newCachedThreadPool();

    protected SystemSettings systemSettings;


    public SystemManagerImpl()
    {
        this.systemSettings = getSystemSettings();
    }


    protected SystemSettings getSystemSettings()
    {
        return new SystemSettings();
    }


    public void addListener( SettingsListener listener )
    {

        if ( listener != null )
        {
            listeners.add( listener );
        }
    }


    public void removeListener( SettingsListener listener )
    {
        if ( listener != null )
        {
            listeners.remove( listener );
        }
    }


    public void dispose()
    {
        notifierPool.shutdown();
        listeners.clear();
    }


    protected void notifyListeners()
    {
        for ( final SettingsListener listener : listeners )
        {
            notifierPool.execute( new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        listener.settingsChanged();
                    }
                    catch ( Exception ignore )
                    {
                    }
                }
            } );
        }
    }


    @Override
    public SystemInfo getSystemInfo()
    {
        SystemInfo pojo = new SystemInfoPojo();

        pojo.setGitCommitId( SubutaiInfo.getCommitId() );
        pojo.setGitBranch( SubutaiInfo.getBranch() );
        pojo.setGitCommitUserName( SubutaiInfo.getCommitterUserName() );
        pojo.setGitCommitUserEmail( SubutaiInfo.getCommitterUserEmail() );
        pojo.setGitBuildUserName( SubutaiInfo.getBuilderUserName() );
        pojo.setGitBuildUserEmail( SubutaiInfo.getBuilderUserEmail() );
        pojo.setGitBuildTime( SubutaiInfo.getBuildTime() );
        pojo.setProjectVersion( SubutaiInfo.getVersion() );

        try
        {
            ResourceHost host = peerManager.getLocalPeer().getManagementHost();
            pojo.setRhVersion( host.getRhVersion().replace( "Subutai version", "" ).trim() );
            pojo.setP2pVersion( host.getP2pVersion().replace( "p2p Cloud project", "" ).trim() );
        }
        catch ( HostNotFoundException | ResourceHostException e )
        {
            e.printStackTrace();
            pojo.setRhVersion( "No RH connected" );
            return pojo;
        }

        return pojo;
    }


    @Override
    public void setPeerSettings()
    {
        identityManager.setPeerOwner( identityManager.getActiveUser() );
    }


    @Override
    public PeerSettings getPeerSettings()
    {
        String peerOwnerId = identityManager.getPeerOwnerId();
        User user = identityManager.getUserByKeyId( peerOwnerId );

        PeerSettings pojo = new PeerSettingsPojo();

        pojo.setPeerOwnerId( peerOwnerId );
        pojo.setUserPeerOwnerName( user.getUserName() );

        return pojo;
    }


    @Override
    public NetworkSettings getNetworkSettings() throws ConfigurationException
    {
        NetworkSettings pojo = new NetworkSettingsPojo();

        pojo.setPublicUrl( systemSettings.getPublicUrl() );
        pojo.setPublicSecurePort( systemSettings.getPublicSecurePort() );
        pojo.setStartRange( systemSettings.getP2pPortStartRange() );
        pojo.setEndRange( systemSettings.getP2pPortEndRange() );

        return pojo;
    }


    @Override
    public void setNetworkSettings( final String publicUrl, final String publicSecurePort, final String startRange,
                                    final String endRange ) throws ConfigurationException
    {
        try
        {
            peerManager.setPublicUrl( peerManager.getLocalPeer().getId(), publicUrl,
                    Integer.parseInt( publicSecurePort ) );

            systemSettings.setP2pPortRange( Integer.parseInt( startRange ), Integer.parseInt( endRange ) );
        }
        catch ( Exception e )
        {
            throw new ConfigurationException( e );
        }
    }


    @Override
    public AdvancedSettings getAdvancedSettings()
    {
        AdvancedSettings pojo = new AdvancedSettingsPojo();

        String content;
        try
        {
            content = new String( Files.readAllBytes(
                    Paths.get( System.getenv( "SUBUTAI_APP_DATA_PATH" ) + "/data/log/karaf.log" ) ) );
            pojo.setKarafLogs( content );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        return pojo;
    }


    @Override
    public SystemInfo getManagementUpdates()
    {
        SystemInfo info = getSystemInfo();

        try
        {
            ResourceHost host = peerManager.getLocalPeer().getManagementHost();

            CommandResult result = host.execute( new RequestBuilder( "subutai update management -c" ) );

            if ( result.getExitCode() == 0 )
            {
                info.setUpdatesAvailable( true );
            }
            else
            {
                info.setUpdatesAvailable( false );
            }
        }
        catch ( HostNotFoundException e )
        {
            e.printStackTrace();
            info.setRhVersion( "No RH connected" );
            return info;
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }

        return info;
    }


    @Override
    public boolean updateManagement()
    {
        try
        {
            ResourceHost host = peerManager.getLocalPeer().getManagementHost();

            host.execute( new RequestBuilder( "subutai update management" ).withTimeout( 10000 ) );

            return true;
        }
        catch ( HostNotFoundException e )
        {
            e.printStackTrace();
            return false;
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
            return false;
        }
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }
}
