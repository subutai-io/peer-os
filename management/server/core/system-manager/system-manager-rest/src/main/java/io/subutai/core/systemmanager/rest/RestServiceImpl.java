package io.subutai.core.systemmanager.rest;


import javax.ws.rs.core.Response;

import io.subutai.common.about.SubutaiInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.settings.KurjunSettings;
import io.subutai.common.settings.PeerSettings;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.systemmanager.rest.pojo.KurjunSettingsPojo;
import io.subutai.core.systemmanager.rest.pojo.PeerSettingsPojo;
import io.subutai.core.systemmanager.rest.pojo.VersionPojo;


/**
 * Created by ermek on 2/6/16.
 */
public class RestServiceImpl implements RestService
{
    private SystemManager systemManager;
    private PeerManager peerManager;


    @Override
    public Response getSubutaiInfo()
    {
        VersionPojo pojo = new VersionPojo();

        pojo.setGitCommitId( SubutaiInfo.getCommitId() );
        pojo.setGitBranch( SubutaiInfo.getBranch() );
        pojo.setGitCommitUserName( SubutaiInfo.getCommitterUserName() );
        pojo.setGitCommitUserEmail( SubutaiInfo.getCommitterUserEmail() );
        pojo.setGitBuildUserName( SubutaiInfo.getBuilderUserName() );
        pojo.setGitBuildUserEmail( SubutaiInfo.getBuilderUserEmail() );
        pojo.setGitBuildTime( SubutaiInfo.getBuildTime() );
        pojo.setProjectVersion( SubutaiInfo.getVersion() );

        String projectInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( projectInfo ).build();
    }


    @Override
    public Response getPeerSettings()
    {
        PeerSettingsPojo pojo = new PeerSettingsPojo();

        pojo.setExternalIpInterface( PeerSettings.getExternalIpInterface() );
        pojo.setEncryptionState( PeerSettings.getEncryptionState() );
        pojo.setRestEncryptionState( PeerSettings.getRestEncryptionState() );
        pojo.setIntegrationState( PeerSettings.getIntegrationState() );
        pojo.setKeyTrustCheckState( PeerSettings.getKeyTrustCheckState() );

        String peerSettingsInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( peerSettingsInfo ).build();
    }


    @Override
    public Response getKurjunSettings()
    {
        KurjunSettingsPojo pojo = new KurjunSettingsPojo();

        pojo.setGlobalKurjunUrls( KurjunSettings.getGlobalKurjunUrls() );

        String kurjunSettingsInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( kurjunSettingsInfo ).build();
    }


    @Override
    public Response getPeerPolicy()
    {
        PeerPolicy peerPolicy =  peerManager.getPolicy( peerManager.getLocalPeer().getId() );

        return Response.status( Response.Status.OK ).entity( peerPolicy ).build();
    }


    public void setSystemManager( final SystemManager systemManager )
    {
        this.systemManager = systemManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }
}
