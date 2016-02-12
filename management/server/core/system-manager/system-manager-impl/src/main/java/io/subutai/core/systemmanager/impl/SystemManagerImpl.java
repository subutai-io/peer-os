package io.subutai.core.systemmanager.impl;


import io.subutai.common.settings.SubutaiInfo;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.kurjun.api.KurjunTransferQuota;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.KurjunSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SecuritySettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;
import io.subutai.core.systemmanager.impl.pojo.NetworkSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.KurjunSettingsPojo;
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
    public void setSecuritySettings( final SecuritySettings settings )
    {
        io.subutai.common.settings.PeerSettings
                .setSettings( settings.getEncryptionState(),
                        settings.getRestEncryptionState(), settings.getIntegrationState(),
                        settings.getKeyTrustCheckState() );
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

        pojo.setGlobalKurjunUrls( String.valueOf( io.subutai.common.settings.KurjunSettings.getGlobalKurjunUrls() ) );

        return pojo;
    }


    @Override
    public void setKurjunSettings( final KurjunSettings settings )
    {
        io.subutai.common.settings.KurjunSettings.setSettings( settings.getGlobalKurjunUrls() );
    }


    @Override
    public SystemInfo getSystemInfo()
    {
        SystemInfo pojo = new SystemInfoPojo();

        pojo.setGitCommitId( String.valueOf( SubutaiInfo.getCommitId() ) );
        pojo.setGitBranch( String.valueOf( SubutaiInfo.getBranch() ) );
        pojo.setGitCommitUserName( String.valueOf( SubutaiInfo.getCommitterUserName() ) );
        pojo.setGitCommitUserEmail( String.valueOf( SubutaiInfo.getCommitterUserEmail() ) );
        pojo.setGitBuildUserName( String.valueOf( SubutaiInfo.getBuilderUserName() ) );
        pojo.setGitBuildUserEmail( String.valueOf( SubutaiInfo.getBuilderUserEmail() ) );
        pojo.setGitBuildTime( String.valueOf( SubutaiInfo.getBuildTime() ) );
        pojo.setProjectVersion( String.valueOf( SubutaiInfo.getVersion() ) );

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
    public NetworkSettings getNetworkSettings()
    {
        NetworkSettings pojo = new NetworkSettingsPojo();

        pojo.setExternalIpInterface(
                String.valueOf( io.subutai.common.settings.PeerSettings.getExternalIpInterface() ) );
        pojo.setOpenPort( io.subutai.common.settings.ChannelSettings.OPEN_PORT );
        pojo.setSecurePortX1( io.subutai.common.settings.ChannelSettings.SECURE_PORT_X1 );
        pojo.setSecurePortX2( io.subutai.common.settings.ChannelSettings.SECURE_PORT_X2 );
        pojo.setSecurePortX3( io.subutai.common.settings.ChannelSettings.SECURE_PORT_X3 );
        pojo.setSpecialPortX1( io.subutai.common.settings.ChannelSettings.SPECIAL_PORT_X1 );

        return pojo;
    }


    @Override
    public void setNetworkSettings( final NetworkSettings settings )
    {
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
