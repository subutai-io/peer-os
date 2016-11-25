package io.subutai.core.systemmanager.impl;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.settings.SubutaiInfo;
import io.subutai.common.settings.SystemSettings;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;
import io.subutai.core.systemmanager.api.pojo.UpdateDto;
import io.subutai.core.systemmanager.impl.dao.UpdateDao;
import io.subutai.core.systemmanager.impl.entity.UpdateEntity;
import io.subutai.core.systemmanager.impl.pojo.AdvancedSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.NetworkSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.PeerSettingsPojo;
import io.subutai.core.systemmanager.impl.pojo.SystemInfoPojo;


public class SystemManagerImpl implements SystemManager
{
    private static final Logger LOG = LoggerFactory.getLogger( SystemManagerImpl.class );

    private IdentityManager identityManager;
    private PeerManager peerManager;
    private DaoManager daoManager;
    private UpdateDao updateDao;

    private SystemSettings systemSettings;


    public SystemManagerImpl()
    {
        this.systemSettings = getSystemSettings();
    }


    private SystemSettings getSystemSettings()
    {
        return new SystemSettings();
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
            LOG.warn( e.getMessage() );

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
    public NetworkSettings getNetworkSettings() throws ConfigurationException
    {
        NetworkSettings pojo = new NetworkSettingsPojo();

        pojo.setPublicUrl( systemSettings.getPublicUrl() );
        pojo.setPublicSecurePort( systemSettings.getPublicSecurePort() );
        pojo.setStartRange( systemSettings.getP2pPortStartRange() );
        pojo.setEndRange( systemSettings.getP2pPortEndRange() );

        return pojo;
    }


    @Override
    public void setNetworkSettings( final String publicUrl, final String publicSecurePort, final String startRange,
                                    final String endRange ) throws ConfigurationException
    {
        try
        {
            peerManager.setPublicUrl( peerManager.getLocalPeer().getId(), publicUrl,
                    Integer.parseInt( publicSecurePort ) );

            systemSettings.setP2pPortRange( Integer.parseInt( startRange ), Integer.parseInt( endRange ) );
        }
        catch ( Exception e )
        {
            throw new ConfigurationException( e );
        }
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
            LOG.warn( e.getMessage() );
        }

        return pojo;
    }


    @Override
    public SystemInfo getManagementUpdates()
    {
        SystemInfo info = getSystemInfo();

        try
        {
            ResourceHost host = peerManager.getLocalPeer().getManagementHost();

            CommandResult result = host.execute( new RequestBuilder( "subutai update management -c" ) );

            if ( result.hasSucceeded() )
            {
                info.setUpdatesAvailable( true );
            }
            else
            {
                info.setUpdatesAvailable( false );
            }
        }
        catch ( HostNotFoundException e )
        {
            LOG.warn( e.getMessage() );

            info.setRhVersion( "No RH connected" );

            return info;
        }
        catch ( CommandException e )
        {
            LOG.warn( e.getMessage() );
        }

        return info;
    }


    @Override
    public boolean updateManagement()
    {
        try
        {
            ResourceHost host = peerManager.getLocalPeer().getManagementHost();

            UpdateEntity updateEntity = new UpdateEntity( SubutaiInfo.getVersion(), SubutaiInfo.getCommitId() );

            updateDao.persist( updateEntity );

            CommandResult result =
                    host.execute( new RequestBuilder( "subutai update management" ).withTimeout( 10000 ) );

            if ( result.hasSucceeded() )
            {
                updateEntity.setCurrentVersion( "No change" );

                updateEntity.setCurrentCommitId( "Other (system) components updated" );

                updateDao.update( updateEntity );
            }
            else
            {
                updateDao.remove( updateEntity.getId() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error updating Management: {}", e.getMessage() );

            return false;
        }

        return true;
    }


    public void init()
    {
        this.updateDao = new UpdateDao( daoManager.getEntityManagerFactory() );

        UpdateEntity updateEntity = updateDao.getLast();

        if ( updateEntity != null && updateEntity.getCurrentVersion() == null )
        {
            //TODO check if prev and current version/commit id is the same
            updateEntity.setCurrentVersion( SubutaiInfo.getVersion() );

            updateEntity.setCurrentCommitId( SubutaiInfo.getCommitId() );

            updateDao.update( updateEntity );
        }
    }


    @Override
    public List<UpdateDto> getUpdates()
    {
        List<UpdateDto> updateDtos = new ArrayList<>();

        List<UpdateEntity> updateEntities = updateDao.getLast( 20 );

        for ( UpdateEntity updateEntity : updateEntities )
        {
            updateDtos.add( new UpdateDto( updateEntity.getUpdateDate(), updateEntity.getPrevVersion(),
                    updateEntity.getCurrentVersion(), updateEntity.getPrevCommitId(),
                    updateEntity.getCurrentCommitId() ) );
        }


        return updateDtos;
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
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
