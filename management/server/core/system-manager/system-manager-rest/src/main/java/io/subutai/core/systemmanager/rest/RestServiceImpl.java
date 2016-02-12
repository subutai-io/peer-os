package io.subutai.core.systemmanager.rest;


import javax.ws.rs.core.Response;

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
    public Response getPeerPolicy()
    {
        PeerPolicy peerPolicy = peerManager.getPolicy( peerManager.getLocalPeer().getId() );

        return Response.status( Response.Status.OK ).entity( peerPolicy ).build();
    }


    @Override
    public Response getNetworkSettings()
    {
        NetworkSettings pojo = systemManager.getNetworkSettings();
        String networkSettingsInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( networkSettingsInfo ).build();
    }


    @Override
    public Response getSecuritySettings()
    {
        SecuritySettings pojo = systemManager.getSecuritySettings();
        String securitySettingsInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( securitySettingsInfo ).build();
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
