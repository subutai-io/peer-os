package io.subutai.core.systemmanager.impl;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.systemmanager.impl.pojo.*;
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
import io.subutai.core.kurjun.api.KurjunTransferQuota;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;
import io.subutai.core.systemmanager.api.pojo.KurjunSettings;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;
import io.subutai.hub.share.dto.SystemConfDto;
import io.subutai.hub.share.dto.SystemConfigurationType;


public class SystemManagerImpl implements SystemManager
{
    private static final Logger LOG = LoggerFactory.getLogger( SystemManagerImpl.class );

    private TemplateManager templateManager;
    private IdentityManager identityManager;
    private PeerManager peerManager;

    protected Set<SettingsListener> listeners =
            Collections.newSetFromMap( new ConcurrentHashMap<SettingsListener, Boolean>() );

    protected ExecutorService notifierPool = Executors.newCachedThreadPool();


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


    public SystemManagerImpl()
    {

    }


    @Override
    public KurjunSettings getKurjunSettings() throws ConfigurationException
    {
        KurjunSettings pojo = new KurjunSettingsPojo();

        KurjunTransferQuota publicTransferQuota = templateManager.getTransferQuota( "public" );
        KurjunTransferQuota trustTransferQuota = templateManager.getTransferQuota( "trust" );
        Long publicDiskQuota = templateManager.getDiskQuota( "public" );
        Long trustDiskQuota = templateManager.getDiskQuota( "trust" );

        if ( publicDiskQuota != null && publicTransferQuota != null )
        {
            pojo.setPublicDiskQuota( publicDiskQuota );
            pojo.setPublicThreshold( publicTransferQuota.getThreshold() );
            pojo.setPublicTimeFrame( publicTransferQuota.getTimeFrame() );
            pojo.setPublicTimeUnit( publicTransferQuota.getTimeUnit() );
        }

        if ( trustDiskQuota != null && trustTransferQuota != null )
        {
            pojo.setTrustDiskQuota( trustDiskQuota );
            pojo.setTrustThreshold( trustTransferQuota.getThreshold() );
            pojo.setTrustTimeFrame( trustTransferQuota.getTimeFrame() );
            pojo.setTrustTimeUnit( trustTransferQuota.getTimeUnit() );
        }

        pojo.setGlobalKurjunUrls( SystemSettings.getGlobalKurjunUrls() );
        pojo.setLocalKurjunUrls( SystemSettings.getLocalKurjunUrls() );

        return pojo;
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

            Map p2pVersions = new HashMap<String, P2PStats>();
            peerManager.getLocalPeer().getResourceHosts().stream().forEach( rh -> {
                try
                {
                    String status = "";
                    try
                    {
                        status = rh.execute( new RequestBuilder( "p2p status" ) ).getStdOut();
                    }
                    catch (CommandException e)
                    {
                        // @todo add logger
                        e.printStackTrace();
                    }

                    if( status.length() > 0 )
                    {
                        p2pVersions.put( rh.getId(), new P2PStats(rh.getId(), rh.getRhVersion(), rh.getP2pVersion(), status) );
                    }
                    else
                    {
                        p2pVersions.put( rh.getId(), new P2PStats(rh.getId()) );
                    }
                } catch (ResourceHostException e)
                {
                    // @todo add logger
                    e.printStackTrace();
                    p2pVersions.put( rh.getId(), new P2PStats(rh.getId()) );
                }
            } );


            pojo.setPeerP2PVersions( p2pVersions );
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
    public void setNetworkSettings( final String publicUrl, final String publicSecurePort )
            throws ConfigurationException
    {


        try
        {
            peerManager.setPublicUrl( peerManager.getLocalPeer().getId(), publicUrl,
                    Integer.parseInt( publicSecurePort ) );
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
    public void setKurjunSettingsUrls( final String[] globalKurjunUrls, final String[] localKurjunUrls )
            throws ConfigurationException
    {
        SystemSettings.setGlobalKurjunUrls( globalKurjunUrls );
        SystemSettings.setLocalKurjunUrls( localKurjunUrls );
    }


    @Override
    public boolean setKurjunSettingsQuotas( final long publicDiskQuota, final long publicThreshold,
                                            final long publicTimeFrame, final long trustDiskQuota,
                                            final long trustThreshold, final long trustTimeFrame )
    {
        templateManager.setDiskQuota( publicDiskQuota, "public" );
        templateManager.setDiskQuota( trustDiskQuota, "trust" );

        KurjunTransferQuota publicTransferQuota =
                new KurjunTransferQuota( publicThreshold, publicTimeFrame, TimeUnit.HOURS );
        KurjunTransferQuota trustTransferQuota =
                new KurjunTransferQuota( trustThreshold, trustTimeFrame, TimeUnit.HOURS );

        boolean isPublicQuotaSaved = templateManager.setTransferQuota( publicTransferQuota, "public" );
        boolean isTrustQuotaSaved = templateManager.setTransferQuota( trustTransferQuota, "trust" );

        return isPublicQuotaSaved && isTrustQuotaSaved;
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

            CommandResult result =
                    host.execute( new RequestBuilder( "subutai update management" ).withTimeout( 10000 ) );

            //            return result.hasSucceeded();
            result.getExitCode();
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


    @Override
    public NetworkSettings getNetworkSettings() throws ConfigurationException
    {
        NetworkSettings pojo = new NetworkSettingsPojo();

        pojo.setPublicUrl( SystemSettings.getPublicUrl() );
        pojo.setPublicSecurePort( SystemSettings.getPublicSecurePort() );

        return pojo;
    }


    public void setTemplateManager( final TemplateManager templateManager )
    {
        this.templateManager = templateManager;
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
