package io.subutai.core.systemmanager.impl;


import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.settings.SubutaiInfo;
import io.subutai.common.settings.SystemSettings;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.kurjun.api.KurjunTransferQuota;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.systemmanager.api.pojo.KurjunSettings;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;
import io.subutai.core.systemmanager.impl.pojo.KurjunSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.NetworkSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.PeerSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.SystemInfoPojo;


/**
 * Created by ermek on 2/6/16.
 */
public class SystemManagerImpl implements SystemManager
{
    private TemplateManager templateManager;
    private IdentityManager identityManager;


    //    public SystemManagerImpl( final String externalInterfaceName, final String publicURL,
    //                              final boolean isRegisteredToHub, final boolean encryptionEnabled,
    //                              final boolean restEncryptionEnabled, final boolean integrationEnabled,
    //                              final boolean keyTrustCheckEnabled, final String globalKurjunUrls,final int
    // openPort, final int securePortX1,
    //                              final int securePortX2, final int securePortX3, final int specialPortX1  )


    public SystemManagerImpl( /*final String globalKurjunUrls, final int securePortX1, final int securePortX2,
                              final int securePortX3*/ ) throws ConfigurationException

    {
//        Preconditions.checkNotNull( globalKurjunUrls, "Invalid Global Kurjun URLs could not be null." );
//        Preconditions.checkArgument( globalKurjunUrls.length > 0, "Invalid Global Kurjun URLs could not be empty." );

        //        SystemSettings.setExternalIpInterface( externalInterfaceName );
        //        SystemSettings.setPublicUrl( publicURL );
        //        SystemSettings.setRegisterToHubState( isRegisteredToHub );
        //
        //        SystemSettings.setEncryptionState( encryptionEnabled );
        //        SystemSettings.setRestEncryptionState( restEncryptionEnabled );
        //        SystemSettings.setIntegrationState( integrationEnabled );
        //        SystemSettings.setKeyTrustCheckState( keyTrustCheckEnabled );


//        SystemSettings.setGlobalKurjunUrls( globalKurjunUrls );
//        //        SystemSettings.setOpenPort( openPort );
//        SystemSettings.setSecurePortX1( securePortX1 );
//        SystemSettings.setSecurePortX2( securePortX2 );
//        SystemSettings.setSecurePortX3( securePortX3 );
        //        SystemSettings.setSpecialPortX1( specialPortX1 );
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
        pojo.setRegisteredToHub( io.subutai.common.settings.SystemSettings.isRegisteredToHub() );

        return pojo;
    }


    @Override
    public void setNetworkSettings( final String securePortX1, final String securePortX2, final String securePortX3 )
    {
        SystemSettings.setSecurePortX1( Integer.parseInt( securePortX1 ) );
        SystemSettings.setSecurePortX2( Integer.parseInt( securePortX2 ) );
        SystemSettings.setSecurePortX3( Integer.parseInt( securePortX3 ) );
    }


    @Override
    public boolean setKurjunSettings( final String[] globalKurjunUrls, final long publicDiskQuota,
                                      final long publicThreshold, final long publicTimeFrame, final long trustDiskQuota,
                                      final long trustThreshold, final long trustTimeFrame )
            throws ConfigurationException
    {
        SystemSettings.setGlobalKurjunUrls( globalKurjunUrls );

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

        pojo.setSecurePortX1( io.subutai.common.settings.SystemSettings.getSecurePortX1() );
        pojo.setSecurePortX2( io.subutai.common.settings.SystemSettings.getSecurePortX2() );
        pojo.setSecurePortX3( io.subutai.common.settings.SystemSettings.getSecurePortX3() );

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
