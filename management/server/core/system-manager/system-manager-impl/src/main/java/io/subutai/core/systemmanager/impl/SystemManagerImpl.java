package io.subutai.core.systemmanager.impl;


import java.util.concurrent.TimeUnit;

import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.SubutaiInfo;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.kurjun.api.KurjunTransferQuota;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.systemmanager.api.pojo.KurjunSettings;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SecuritySettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;
import io.subutai.core.systemmanager.impl.pojo.KurjunSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.NetworkSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.PeerSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.SecuritySettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.SystemInfoPojo;


/**
 * Created by ermek on 2/6/16.
 */
public class SystemManagerImpl implements SystemManager
{
    private TemplateManager templateManager;
    private IdentityManager identityManager;


    @Override
    public SecuritySettings getSecuritySettings()
    {
        SecuritySettings pojo = new SecuritySettingsPojo();

        pojo.setEncryptionState( io.subutai.common.settings.PeerSettings.getEncryptionState() );
        pojo.setRestEncryptionState( io.subutai.common.settings.PeerSettings.getRestEncryptionState() );
        pojo.setIntegrationState( io.subutai.common.settings.PeerSettings.getIntegrationState() );
        pojo.setKeyTrustCheckState( io.subutai.common.settings.PeerSettings.getKeyTrustCheckState() );

        return pojo;
    }


    @Override
    public KurjunSettings getKurjunSettings()
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

        pojo.setGlobalKurjunUrls( io.subutai.common.settings.KurjunSettings.getGlobalKurjunUrls() );

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
        pojo.setRegisteredToHub( io.subutai.common.settings.PeerSettings.isRegisteredToHub() );

        return pojo;
    }


    @Override
    public void setNetworkSettings( final String externalIpInterface, final String openPort, final String securePortX1,
                                    final String securePortX2, final String securePortX3, final String specialPortX1 )
    {
        io.subutai.common.settings.PeerSettings.setExternalIpInterface( externalIpInterface );
        ChannelSettings.setOpenPort( Integer.parseInt( openPort ) );
        ChannelSettings.setSecurePortX1( Integer.parseInt( securePortX1 ) );
        ChannelSettings.setSecurePortX2( Integer.parseInt( securePortX2 ) );
        ChannelSettings.setSecurePortX3( Integer.parseInt( securePortX3 ) );
        ChannelSettings.setSpecialPortX1( Integer.parseInt( specialPortX1 ) );
    }


    @Override
    public void setSecuritySettings( final boolean encryptionEnabled, final boolean restEncryptionEnabled,
                                     final boolean integrationEnabled, final boolean keyTrustCheckEnabled )
    {
        io.subutai.common.settings.PeerSettings.setEncryptionState( encryptionEnabled );
        io.subutai.common.settings.PeerSettings.setRestEncryptionState( restEncryptionEnabled );
        io.subutai.common.settings.PeerSettings.setIntegrationState( integrationEnabled );
        io.subutai.common.settings.PeerSettings.setKeyTrustCheckState( keyTrustCheckEnabled );
    }


    @Override
    public boolean setKurjunSettings( final String globalKurjunUrls, final long publicDiskQuota,
                                      final long publicThreshold, final long publicTimeFrame, final long trustDiskQuota,
                                      final long trustThreshold, final long trustTimeFrame )
    {
        io.subutai.common.settings.KurjunSettings.setSettings( globalKurjunUrls );

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
    public NetworkSettings getNetworkSettings()
    {
        NetworkSettings pojo = new NetworkSettingsPojo();

        pojo.setExternalIpInterface( io.subutai.common.settings.PeerSettings.getExternalIpInterface() );
        pojo.setOpenPort( io.subutai.common.settings.ChannelSettings.OPEN_PORT );
        pojo.setSecurePortX1( io.subutai.common.settings.ChannelSettings.SECURE_PORT_X1 );
        pojo.setSecurePortX2( io.subutai.common.settings.ChannelSettings.SECURE_PORT_X2 );
        pojo.setSecurePortX3( io.subutai.common.settings.ChannelSettings.SECURE_PORT_X3 );
        pojo.setSpecialPortX1( io.subutai.common.settings.ChannelSettings.SPECIAL_PORT_X1 );

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
}
