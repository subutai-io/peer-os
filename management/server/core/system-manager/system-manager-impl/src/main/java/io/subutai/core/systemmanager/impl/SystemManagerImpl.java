package io.subutai.core.systemmanager.impl;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.exception.ActionFailedException;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SubutaiInfo;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.bazaarmanager.api.BazaarManager;
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
        SystemInfoPojo pojo = new SystemInfoPojo();

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
            pojo.setOsName( host.getOsName().trim() );
        }
        catch ( HostNotFoundException | ResourceHostException e )
        {
            LOG.error( "Error getting system info: {}", e.getMessage() );

            if ( StringUtils.isBlank( pojo.getRhVersion() ) )
            {
                pojo.setRhVersion( "Failed to obtain version" );
            }

            if ( StringUtils.isBlank( pojo.getP2pVersion() ) )
            {
                pojo.setP2pVersion( "Failed to obtain version" );
            }

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
        NetworkSettingsPojo pojo = new NetworkSettingsPojo();

        PeerInfo localPeerInfo = peerManager.getLocalPeer().getPeerInfo();
        pojo.setPublicUrl( localPeerInfo.getPublicUrl() );
        pojo.setPublicSecurePort( localPeerInfo.getPublicSecurePort() );
        pojo.setUseRhIp( !localPeerInfo.isManualSetting() );
        pojo.setStartRange( Integer.parseInt( Common.P2P_PORT_RANGE_START ) );
        pojo.setEndRange( Integer.parseInt( Common.P2P_PORT_RANGE_END ) );
        pojo.setBazaarIp( Common.BAZAAR_IP );

        return pojo;
    }


    @Override
    @RolesAllowed( "System-Management|Update" )
    public void setNetworkSettings( final String publicUrl, final String publicSecurePort, final boolean useRhIp )
            throws ConfigurationException
    {
        try
        {
            peerManager
                    .setPublicUrl( peerManager.getLocalPeer().getId(), publicUrl, Integer.parseInt( publicSecurePort ),
                            useRhIp );
        }
        catch ( Exception e )
        {
            throw new ConfigurationException( e );
        }
    }


    @Override
    @RolesAllowed( "System-Management|Read" )
    public AdvancedSettings getAdvancedSettings( String logFile )
    {
        AdvancedSettingsPojo pojo = new AdvancedSettingsPojo();

        String content;
        try
        {
            Path karafLogDirPath = Paths.get( System.getenv( "SUBUTAI_APP_DATA_PATH" ), "/data/log/" );

            Path currentKarafLogFilePath;
            if ( Strings.isNullOrEmpty( logFile ) )
            {
                currentKarafLogFilePath = karafLogDirPath.resolve( "karaf.log" );
            }
            else
            {
                currentKarafLogFilePath = karafLogDirPath.resolve( logFile );
            }

            content = new String( Files.readAllBytes( currentKarafLogFilePath ) );

            pojo.setKarafLogs( content );

            File[] karafLogFiles = karafLogDirPath.toFile().listFiles( new FileFilter()
            {
                @Override
                public boolean accept( final File pathname )
                {
                    return pathname.isFile() && pathname.getName().startsWith( "karaf.log" );
                }
            } );

            assert karafLogFiles != null;

            Arrays.sort( karafLogFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE );

            List<String> karafLogFileNames = Lists.newArrayList();

            for ( File karafLogFile : karafLogFiles )
            {
                karafLogFileNames.add( karafLogFile.getName() );
            }

            pojo.setKarafLogFiles( karafLogFileNames );
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
        SystemInfoPojo info = ( SystemInfoPojo ) getSystemInfo();

        try
        {
            ResourceHost host = peerManager.getLocalPeer().getManagementHost();

            CommandResult result = host.execute(
                    new RequestBuilder( "subutai update management -c ; subutai update rh -c" ).withTimeout(
                            ( int ) TimeUnit.MINUTES.toSeconds(
                                    Common.MH_UPDATE_CHECK_TIMEOUT_MIN + Common.RH_UPDATE_CHECK_TIMEOUT_MIN ) ) );

            if ( result.getStdOut().contains( "Update is available" ) )
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

        notifyBazaarThatPeerIsOffline();

        isUpdateInProgress = true;

        try
        {
            ResourceHost host = peerManager.getLocalPeer().getManagementHost();

            UpdateEntity updateEntity =
                    new UpdateEntity( SubutaiInfo.getVersion(), SubutaiInfo.getCommitId(), SubutaiInfo.getBuildTime() );

            updateDao.persist( updateEntity );

            boolean rhUpdated = updateRH();

            CommandResult result = host.execute( new RequestBuilder( "subutai update management" )
                    .withTimeout( ( int ) TimeUnit.MINUTES.toSeconds( Common.MH_UPDATE_TIMEOUT_MIN ) ) );

            boolean mhUpdated = !result.getStdOut().contains( "No update is available" ) && result.hasSucceeded();

            if ( mhUpdated || rhUpdated )
            {
                updateEntity.setCurrentVersion( "No change" );

                updateEntity.setCurrentCommitId( "System components updated" );

                updateDao.update( updateEntity );
            }
            else if ( !result.hasTimedOut() )
            {
                updateDao.remove( updateEntity.getId() );
            }

            return mhUpdated;
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
    public boolean isUpdateInProgress()
    {
        return isUpdateInProgress;
    }


    @Override
    public boolean isEnvironmentWorkflowInProgress()
    {
        EnvironmentManager environmentManager = ServiceLocator.lookup( EnvironmentManager.class );

        return !environmentManager.getActiveWorkflows().isEmpty() || !peerManager.getLocalPeer().getTasks().isEmpty();
    }


    public void notifyBazaarThatPeerIsOffline()
    {
        BazaarManager bazaarManager = ServiceLocator.lookup( BazaarManager.class );
        bazaarManager.notifyBazaarThatPeerIsOffline();
    }


    @Override
    public String getBazaarIp()
    {
        return Common.BAZAAR_IP;
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

                if ( Objects.equals( updateEntity.getBuildTime(), SubutaiInfo.getBuildTime() ) )
                {
                    updateEntity.setCurrentCommitId( "Probably update was interrupted" );
                }
                else
                {
                    updateEntity.setCurrentCommitId( "Console was rebuilt" );
                }
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


    private boolean updateRH()
    {
        try
        {
            return peerManager.getLocalPeer().getManagementHost().update();
        }
        catch ( HostNotFoundException e )
        {
            LOG.error( "Error updating MH: {}", e.getMessage() );
        }

        return false;
    }
}
