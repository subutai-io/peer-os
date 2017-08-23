package io.subutai.core.systemmanager.impl;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.exception.ActionFailedException;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SubutaiInfo;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.environment.api.EnvironmentManager;
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
    private static final String UPDATE_IN_PROGRESS_MSG = "Update is in progress";

    private IdentityManager identityManager;
    private PeerManager peerManager;
    private DaoManager daoManager;
    private UpdateDao updateDao;


    private volatile boolean isUpdateInProgress = false;


    @Override
    @RolesAllowed( "System-Management|Read" )
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
    @RolesAllowed( "System-Management|Update" )
    public void setPeerSettings()
    {
        identityManager.setPeerOwner( identityManager.getActiveUser() );
    }


    @Override
    @RolesAllowed( "System-Management|Read" )
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
    @RolesAllowed( "System-Management|Read" )
    public NetworkSettings getNetworkSettings() throws ConfigurationException
    {
        NetworkSettings pojo = new NetworkSettingsPojo();

        pojo.setPublicUrl( peerManager.getLocalPeer().getPeerInfo().getPublicUrl() );
        pojo.setPublicSecurePort( peerManager.getLocalPeer().getPeerInfo().getPublicSecurePort() );
        pojo.setStartRange( Integer.parseInt( Common.P2P_PORT_RANGE_START ) );
        pojo.setEndRange( Integer.parseInt( Common.P2P_PORT_RANGE_END ) );
        pojo.setHubIp( Common.HUB_IP );

        return pojo;
    }


    @Override
    @RolesAllowed( "System-Management|Update" )
    public void setNetworkSettings( final String publicUrl, final String publicSecurePort )
            throws ConfigurationException
    {
        try
        {
            peerManager.setPublicUrl( peerManager.getLocalPeer().getId(), publicUrl,
                    Integer.parseInt( publicSecurePort ) );
        }
        catch ( Exception e )
        {
            throw new ConfigurationException( e );
        }
    }


    @Override
    @RolesAllowed( "System-Management|Read" )
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
    @RolesAllowed( "System-Management|Read" )
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
    @RolesAllowed( "System-Management|Update" )
    public boolean updateManagement()
    {
        if ( isUpdateInProgress || isEnvironmentWorkflowInProgress() )
        {
            return false;
        }

        isUpdateInProgress = true;

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
            else if ( !result.hasTimedOut() )
            {
                updateDao.remove( updateEntity.getId() );
            }

            return result.hasSucceeded();
        }
        catch ( Exception e )
        {
            LOG.error( "Error updating Management: {}", e.getMessage() );

            throw new ActionFailedException( "Error updating Management: " + e.getMessage() );
        }
        finally
        {
            isUpdateInProgress = false;
        }
    }


    @Override
    @RolesAllowed( "System-Management|Read" )
    public boolean isUpdateInProgress()
    {
        return isUpdateInProgress;
    }


    @Override
    public boolean isEnvironmentWorkflowInProgress()
    {
        EnvironmentManager environmentManager = ServiceLocator.lookup( EnvironmentManager.class );
        return !environmentManager.getActiveWorkflows().isEmpty();
    }


    @Override
    public String getHubIp()
    {
        return Common.HUB_IP;
    }


    public void init()
    {
        this.updateDao = new UpdateDao( daoManager.getEntityManagerFactory() );

        UpdateEntity updateEntity = updateDao.getLast();

        if ( updateEntity != null && updateEntity.getCurrentVersion() == null )
        {
            if ( Objects.equals( updateEntity.getPrevCommitId(), SubutaiInfo.getCommitId() ) )
            {
                updateEntity.setCurrentVersion( "No change" );

                updateEntity.setCurrentCommitId( "Probably update was interrupted" );
            }
            else
            {
                updateEntity.setCurrentVersion( SubutaiInfo.getVersion() );

                updateEntity.setCurrentCommitId( SubutaiInfo.getCommitId() );
            }
            updateDao.update( updateEntity );
        }
    }


    @Override
    @RolesAllowed( "System-Management|Read" )
    public List<UpdateDto> getUpdates()
    {
        List<UpdateDto> updateDtos = new ArrayList<>();

        List<UpdateEntity> updateEntities = updateDao.getLast( 20 );

        for ( UpdateEntity updateEntity : updateEntities )
        {
            updateDtos.add( new UpdateDto( updateEntity.getUpdateDate(), updateEntity.getPrevVersion(),
                    updateEntity.getCurrentVersion() == null ? UPDATE_IN_PROGRESS_MSG :
                    updateEntity.getCurrentVersion(), updateEntity.getPrevCommitId(),
                    updateEntity.getCurrentCommitId() == null ? UPDATE_IN_PROGRESS_MSG :
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
