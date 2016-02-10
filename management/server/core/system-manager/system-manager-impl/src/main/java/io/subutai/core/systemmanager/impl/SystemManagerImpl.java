package io.subutai.core.systemmanager.impl;


import io.subutai.common.settings.SubutaiInfo;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.systemmanager.api.pojo.ChannelSettings;
import io.subutai.core.systemmanager.api.pojo.KurjunSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;
import io.subutai.core.systemmanager.impl.pojo.ChannelSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.KurjunSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.PeerSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.SystemInfoPojo;


/**
 * Created by ermek on 2/6/16.
 */
public class SystemManagerImpl implements SystemManager
{
    @Override
    public PeerSettings getPeerSettings()
    {
        PeerSettings pojo = new PeerSettingsPojo();

        pojo.setExternalIpInterface(
                String.valueOf( io.subutai.common.settings.PeerSettings.getExternalIpInterface() ) );
        pojo.setEncryptionState( io.subutai.common.settings.PeerSettings.getEncryptionState() );
        pojo.setRestEncryptionState( io.subutai.common.settings.PeerSettings.getRestEncryptionState() );
        pojo.setIntegrationState( io.subutai.common.settings.PeerSettings.getIntegrationState() );
        pojo.setKeyTrustCheckState( io.subutai.common.settings.PeerSettings.getKeyTrustCheckState() );

        return pojo;
    }


    @Override
    public void setPeerSettings( final PeerSettings settings )
    {
        io.subutai.common.settings.PeerSettings
                .setSettings( settings.getExternalIpInterface(), settings.getEncryptionState(),
                        settings.getRestEncryptionState(), settings.getIntegrationState(),
                        settings.getKeyTrustCheckState() );
    }


    @Override
    public KurjunSettings getKurjunSettings()
    {
        KurjunSettings pojo = new KurjunSettingsPojo();

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
    public ChannelSettings getChannelSettings()
    {
        ChannelSettings pojo = new ChannelSettingsPojo();

        pojo.setOpenPort( io.subutai.common.settings.ChannelSettings.OPEN_PORT );
        pojo.setSecurePortX1( io.subutai.common.settings.ChannelSettings.SECURE_PORT_X1 );
        pojo.setSecurePortX2( io.subutai.common.settings.ChannelSettings.SECURE_PORT_X2 );
        pojo.setSecurePortX3( io.subutai.common.settings.ChannelSettings.SECURE_PORT_X3 );
        pojo.setSpecialPortX1( io.subutai.common.settings.ChannelSettings.SPECIAL_PORT_X1 );

        return pojo;
    }


    @Override
    public void setChannelSettings( final ChannelSettings settings )
    {

    }
}
