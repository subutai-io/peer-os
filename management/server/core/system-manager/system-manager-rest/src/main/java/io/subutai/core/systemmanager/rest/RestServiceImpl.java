package io.subutai.core.systemmanager.rest;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;

import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;
import io.subutai.core.systemmanager.api.pojo.KurjunSettings;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private SystemManager systemManager;
    private PeerManager peerManager;


    @Override
    public Response getSubutaiInfo()
    {
        try
        {
            SystemInfo pojo = systemManager.getSystemInfo();
            String projectInfo = JsonUtil.GSON.toJson( pojo );

            return Response.status( Response.Status.OK ).entity( projectInfo ).build();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
            e.printStackTrace();
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( e.getMessage() ).build();
        }
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
        try
        {
            KurjunSettings pojo = systemManager.getKurjunSettings();
            String kurjunSettingsInfo = JsonUtil.GSON.toJson( pojo );

            return Response.status( Response.Status.OK ).entity( kurjunSettingsInfo ).build();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( e.getMessage() );
            e.printStackTrace();
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response setKurjunSettingsQuotas( final String publicDiskQuota, final String publicThreshold,
                                             final String publicTimeFrame, final String trustDiskQuota,
                                             final String trustThreshold, final String trustTimeFrame )
            throws ConfigurationException
    {

        boolean isSaved = systemManager
                .setKurjunSettingsQuotas( Long.parseLong( publicDiskQuota ), Long.parseLong( publicThreshold ),
                        Long.parseLong( publicTimeFrame ), Long.parseLong( trustDiskQuota ),
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
    public Response setKurjunSettingsUrls( final String globalKurjunUrls, final String localKurjunUrls )
    {

        try
        {
            systemManager.setKurjunSettingsUrls( globalKurjunUrls.split( "," ), localKurjunUrls.split( "," ) );

            systemManager.sendSystemConfigToHub();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( e.getMessage() );
            e.printStackTrace();
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }

        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response getNetworkSettings()
    {
        try
        {
            NetworkSettings pojo = systemManager.getNetworkSettings();
            String networkSettingsInfo = JsonUtil.GSON.toJson( pojo );

            return Response.status( Response.Status.OK ).entity( networkSettingsInfo ).build();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( e.getMessage() );
            e.printStackTrace();
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response setNetworkSettings( final String securePortX1, final String securePortX2, final String securePortX3,
                                        final String publicUrl, final String agentPort, final String publicSecurePort,
                                        final String keyServer )
    {
        //todo remove securePortX3
        try
        {
            systemManager.setNetworkSettings( securePortX1, securePortX2, publicUrl, agentPort, publicSecurePort,
                    keyServer );

            systemManager.sendSystemConfigToHub();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( e.getMessage() );
            e.printStackTrace();
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( e.getMessage() ).build();
        }

        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response getAdvancedSettings()
    {
        AdvancedSettings pojo = systemManager.getAdvancedSettings();
        String advancedSettingsInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( advancedSettingsInfo ).build();
    }


    @Override
    public Response getManagementUpdates()
    {
        SystemInfo pojo = systemManager.getManagementUpdates();
        String subutaiInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( subutaiInfo ).build();
    }


    @Override
    public Response update()
    {
        boolean isSuccessful = systemManager.updateManagement();

        if ( isSuccessful )
        {
            return Response.status( Response.Status.OK ).build();
        }
        else
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
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
