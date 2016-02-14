package io.subutai.core.systemmanager.rest;


import javax.ws.rs.core.Response;

import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.systemmanager.api.pojo.KurjunSettings;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SecuritySettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;


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
        SystemInfo pojo = systemManager.getSystemInfo();
        String projectInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( projectInfo ).build();
    }


    @Override
    public Response setPeerSettings()
    {
        systemManager.setPeerSettings();
        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response getPeerSettings()
    {
        PeerSettings pojo = systemManager.getPeerSettings();
        String peerSettingsInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( peerSettingsInfo ).build();
    }


    @Override
    public Response getKurjunSettings()
    {
        KurjunSettings pojo = systemManager.getKurjunSettings();
        String kurjunSettingsInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( kurjunSettingsInfo ).build();
    }


    @Override
    public Response setKurjunSettings( final String globalKurjunUrls, final String publicDiskQuota,
                                       final String publicThreshold, final String publicTimeFrame,
                                       final String trustDiskQuota, final String trustThreshold,
                                       final String trustTimeFrame )
    {

        boolean isSaved = systemManager.setKurjunSettings( globalKurjunUrls, Long.parseLong( publicDiskQuota ),
                Long.parseLong( publicThreshold ), Long.parseLong( publicTimeFrame ), Long.parseLong( trustDiskQuota ),
                Long.parseLong( trustThreshold ), Long.parseLong( trustTimeFrame ) );

        if ( isSaved )
        {
            return Response.status( Response.Status.OK ).build();
        }
        else
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }


    @Override
    public Response getPeerPolicy()
    {
        PeerPolicy peerPolicy = peerManager.getPolicy( peerManager.getLocalPeer().getId() );

        return Response.status( Response.Status.OK ).entity( peerPolicy ).build();
    }


    @Override
    public Response setPeerPolicy( final String peerId, final String diskUsageLimit, final String cpuUsageLimit,
                                   final String memoryUsageLimit, final String environmentLimit,
                                   final String containerLimit )
    {
        PeerPolicy peerPolicy =
                new PeerPolicy( peerId, Integer.parseInt( diskUsageLimit ), Integer.parseInt( cpuUsageLimit ),
                        Integer.parseInt( memoryUsageLimit ), 90, Integer.parseInt( environmentLimit ),
                        Integer.parseInt( containerLimit ) );
        try
        {
            peerManager.setPolicy( peerId, peerPolicy );
        }
        catch ( PeerException e )
        {
            Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
            e.printStackTrace();
        }
        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response getNetworkSettings()
    {
        NetworkSettings pojo = systemManager.getNetworkSettings();
        String networkSettingsInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( networkSettingsInfo ).build();
    }


    @Override
    public Response setNetworkSettings( final String externalIpInterface, final String openPort,
                                        final String securePortX1, final String securePortX2, final String securePortX3,
                                        final String specialPortX1 )
    {
        systemManager.setNetworkSettings( externalIpInterface, openPort, securePortX1, securePortX2, securePortX3,
                specialPortX1 );
        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response getSecuritySettings()
    {
        SecuritySettings pojo = systemManager.getSecuritySettings();
        String securitySettingsInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( securitySettingsInfo ).build();
    }


    @Override
    public Response setSecuritySettings( final boolean encryptionEnabled, final boolean restEncryptionEnabled,
                                         final boolean integrationEnabled, final boolean keyTrustCheckEnabled )
    {
        systemManager.setSecuritySettings( encryptionEnabled, restEncryptionEnabled, integrationEnabled,
                keyTrustCheckEnabled );
        return Response.status( Response.Status.OK ).build();
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
