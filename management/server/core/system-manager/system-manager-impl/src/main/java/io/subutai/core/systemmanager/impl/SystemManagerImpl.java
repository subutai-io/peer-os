package io.subutai.core.systemmanager.impl;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
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
import io.subutai.core.systemmanager.impl.pojo.AdvancedSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.KurjunSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.NetworkSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.PeerSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.SystemInfoPojo;


public class SystemManagerImpl implements SystemManager
{
    private static final String DEFAULT_KURJUN_REPO = "http://repo.critical-factor.com:8080/rest/kurjun";

    private TemplateManager templateManager;
    private IdentityManager identityManager;
    private PeerManager peerManager;


    public SystemManagerImpl( final String globalKurjunUrls, final int securePortX1, final int securePortX2,
                              final int securePortX3, final String publicUrl ) throws ConfigurationException

    {
        Preconditions.checkNotNull( globalKurjunUrls, "Invalid Global Kurjun URLs could not be null." );

        String[] urls = new String[] { globalKurjunUrls };

        if ( urls.length < 1 )
        {
            urls = new String[] { DEFAULT_KURJUN_REPO };
        }
        validateGlobalKurjunUrls( urls );
        validatePublicUrl( publicUrl );

        SystemSettings.setGlobalKurjunUrls( urls );
        SystemSettings.setSecurePortX1( securePortX1 );
        SystemSettings.setSecurePortX2( securePortX2 );
        SystemSettings.setSecurePortX3( securePortX3 );
        SystemSettings.setPublicUrl( publicUrl );
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
    public SystemInfo getSystemInfo() throws ConfigurationException, HostNotFoundException, CommandException
    {
        CommandResult result = null;

        RequestBuilder requestBuilder = new RequestBuilder( "subutai -v" );
        Host host = peerManager.getLocalPeer().getManagementHost();
        result = peerManager.getLocalPeer().execute( requestBuilder, host );

        String[] version = result.getStdOut().split( "\\s" );

        SystemInfo pojo = new SystemInfoPojo();

        pojo.setGitCommitId( SubutaiInfo.getCommitId() );
        pojo.setGitBranch( SubutaiInfo.getBranch() );
        pojo.setGitCommitUserName( SubutaiInfo.getCommitterUserName() );
        pojo.setGitCommitUserEmail( SubutaiInfo.getCommitterUserEmail() );
        pojo.setGitBuildUserName( SubutaiInfo.getBuilderUserName() );
        pojo.setGitBuildUserEmail( SubutaiInfo.getBuilderUserEmail() );
        pojo.setGitBuildTime( SubutaiInfo.getBuildTime() );
        pojo.setProjectVersion( SubutaiInfo.getVersion() );
        pojo.setRhVersion( version[2] );

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
                                    final String publicUrl, final String agentPort ) throws ConfigurationException
    {
        SystemSettings.setSecurePortX1( Integer.parseInt( securePortX1 ) );
        SystemSettings.setSecurePortX2( Integer.parseInt( securePortX2 ) );
        SystemSettings.setSecurePortX3( Integer.parseInt( securePortX3 ) );
        SystemSettings.setPublicUrl( publicUrl );
        SystemSettings.setAgentPort( Integer.parseInt( agentPort ) );
    }


    @Override
    public AdvancedSettings getAdvancedSettings()
    {
        AdvancedSettings pojo = new AdvancedSettingsPojo();

        String content = null;
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
    public void setKurjunSettingsUrls( final String[] globalKurjunUrls, final String[] localKurjunUrls ) throws ConfigurationException
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
    public NetworkSettings getNetworkSettings() throws ConfigurationException
    {
        NetworkSettings pojo = new NetworkSettingsPojo();

        pojo.setSecurePortX1( SystemSettings.getSecurePortX1() );
        pojo.setSecurePortX2( SystemSettings.getSecurePortX2() );
        pojo.setSecurePortX3( SystemSettings.getSecurePortX3() );
        pojo.setPublicUrl( SystemSettings.getPublicUrl() );
        pojo.setAgentPort( SystemSettings.getAgentPort() );

        return pojo;
    }


    protected static void validateGlobalKurjunUrls( final String[] urls ) throws ConfigurationException
    {
        for ( String url : urls )
        {
            try
            {
                new URL( url );
            }
            catch ( MalformedURLException e )
            {
                throw new ConfigurationException( "Invalid URL: " + url );
            }
        }
    }


    protected static void validatePublicUrl( String publicUrl ) throws ConfigurationException
    {
        try
        {
            new URL( publicUrl );
        }
        catch ( MalformedURLException e )
        {
            throw new ConfigurationException( "Invalid URL: " + publicUrl );
        }
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
