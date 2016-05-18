package io.subutai.core.systemmanager.impl;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;

import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.settings.SettingsListener;
import io.subutai.common.settings.SubutaiInfo;
import io.subutai.common.settings.SystemSettings;
import io.subutai.core.hubmanager.api.HubManager;
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
import io.subutai.core.systemmanager.impl.pojo.AdvancedSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.KurjunSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.NetworkSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.PeerSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.SystemInfoPojo;
import io.subutai.hub.share.dto.SystemConfDto;
import io.subutai.hub.share.dto.SystemConfigurationType;


public class SystemManagerImpl implements SystemManager
{

    private TemplateManager templateManager;
    private IdentityManager identityManager;
    private PeerManager peerManager;
    private HubManager hubManager;

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


    public SystemManagerImpl( final HubManager hubManager )

    {
        this.hubManager = hubManager;
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
    public void setNetworkSettings( final String securePortX1, final String securePortX2, final String securePortX3,
                                    final String publicUrl, final String agentPort, final String publicSecurePort,
                                    final String keyServer )
            throws ConfigurationException
    {
<<<<<<< HEAD
        SystemSettings.setSecurePortX1( Integer.parseInt( securePortX1 ) );
        SystemSettings.setSecurePortX2( Integer.parseInt( securePortX2 ) );
        SystemSettings.setSecurePortX3( Integer.parseInt( securePortX3 ) );
        SystemSettings.setPublicUrl( publicUrl );
        SystemSettings.setAgentPort( Integer.parseInt( agentPort ) );
        SystemSettings.setPublicSecurePort( Integer.parseInt( publicSecurePort ) );
        SystemSettings.setKeyServer( keyServer );

        notifyListeners();
=======
        try
        {
            SystemSettings.setSecurePortX1( Integer.parseInt( securePortX1 ) );
            SystemSettings.setSecurePortX2( Integer.parseInt( securePortX2 ) );
            SystemSettings.setSecurePortX3( Integer.parseInt( securePortX3 ) );
            SystemSettings.setAgentPort( Integer.parseInt( agentPort ) );
            peerManager.setPublicUrl( peerManager.getLocalPeer().getId(), publicUrl,
                    Integer.parseInt( publicSecurePort ) );
        }
        catch ( Exception e )
        {
            throw new ConfigurationException( e );
        }

>>>>>>> 86b16bcf3058d5918fa1d98da561738492eeb4d1
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
    public void sendSystemConfigToHub() throws ConfigurationException
    {
        SystemConfDto dto = new SystemConfDto( SystemConfigurationType.SUBUTAI_SOCIAL );

        KurjunSettings kurjunSettings = getKurjunSettings();
        NetworkSettings networkSettings = getNetworkSettings();

        dto.setGlobalKurjunUrls( kurjunSettings.getGlobalKurjunUrls() );
        dto.setLocalKurjunUrls( kurjunSettings.getLocalKurjunUrls() );
        dto.setSecurePortX1( networkSettings.getSecurePortX1() );
        dto.setSecurePortX2( networkSettings.getSecurePortX2() );
        dto.setSecurePortX3( networkSettings.getSecurePortX3() );
        dto.setPublicSecurePort( networkSettings.getPublicSecurePort() );
        dto.setPublicUrl( networkSettings.getPublicUrl() );
        dto.setAgentPort( networkSettings.getAgentPort() );
        dto.setKeyServer( networkSettings.getKeyServer() );


        hubManager.sendSystemConfiguration( dto );
    }


    @Override
    public NetworkSettings getNetworkSettings() throws ConfigurationException
    {
        NetworkSettings pojo = new NetworkSettingsPojo();

        pojo.setSecurePortX1( SystemSettings.getSecurePortX1() );
        pojo.setSecurePortX2( SystemSettings.getSecurePortX2() );
        pojo.setSecurePortX3( SystemSettings.getSecurePortX3() );
        pojo.setPublicUrl( SystemSettings.getPublicUrl() );
        pojo.setAgentPort( SystemSettings.getAgentPort() );
        pojo.setPublicSecurePort( SystemSettings.getPublicSecurePort() );
        pojo.setKeyServer( SystemSettings.getKeyServer() );

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
