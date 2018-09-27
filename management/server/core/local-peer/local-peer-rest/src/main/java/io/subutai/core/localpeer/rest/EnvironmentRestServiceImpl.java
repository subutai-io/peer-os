package io.subutai.core.localpeer.rest;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.environment.HostAddresses;
import io.subutai.common.environment.PeerTemplatesDownloadProgress;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.Quota;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.protocol.CustomProxyConfig;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKey;
import io.subutai.common.security.SshKeys;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.bazaar.share.quota.ContainerQuota;


/**
 * Environment REST endpoint implementation
 */
public class EnvironmentRestServiceImpl implements EnvironmentRestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( EnvironmentRestServiceImpl.class );

    private LocalPeer localPeer;


    public EnvironmentRestServiceImpl( final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( localPeer );

        this.localPeer = localPeer;
    }


    @Override
    public void destroyContainer( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );

            localPeer.destroyContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public void setContainerHostname( final ContainerId containerId, final String hostname )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );

            localPeer.setContainerHostname( containerId, hostname );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public void startContainer( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );

            localPeer.startContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public void stopContainer( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );

            localPeer.stopContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public ContainerHostState getContainerState( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkNotNull( containerId.getId() );
            return localPeer.getContainerState( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Quota getRawQuota( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkNotNull( containerId.getId() );
            return localPeer.getRawQuota( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    //*********** Quota functions ***************


    @Override
    public SshKeys generateSshKeysForEnvironment( final EnvironmentId environmentId,
                                                  final SshEncryptionType sshKeyType )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );

            return localPeer.readOrCreateSshKeysForEnvironment( environmentId, sshKeyType );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public SshKeys getContainerAuthorizedKeys( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );

            return localPeer.getContainerAuthorizedKeys( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public void addSshKey( final EnvironmentId environmentId, final String sshPublicKey )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( sshPublicKey ) );

            localPeer.addToAuthorizedKeys( environmentId, sshPublicKey );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public void removeSshKey( final EnvironmentId environmentId, final String sshPublicKey )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( sshPublicKey ) );

            localPeer.removeFromAuthorizedKeys( environmentId, sshPublicKey );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response configureSshInEnvironment( final EnvironmentId environmentId, final SshKeys sshKeys )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );
            Preconditions.checkNotNull( sshKeys );
            Preconditions.checkArgument( !sshKeys.isEmpty() );

            localPeer.configureSshInEnvironment( environmentId, sshKeys );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response configureHostsInEnvironment( final EnvironmentId environmentId, final HostAddresses hostAddresses )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );
            Preconditions.checkNotNull( hostAddresses );
            Preconditions.checkArgument( !hostAddresses.isEmpty() );

            localPeer.configureHostsInEnvironment( environmentId, hostAddresses );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response getQuota( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            ContainerQuota resourceValue = localPeer.getQuota( containerId );

            return Response.ok( resourceValue ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response setQuota( final ContainerId containerId, ContainerQuota containerQuota )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkNotNull( containerQuota );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            localPeer.setQuota( containerId, containerQuota );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


/*    @Override
    public Response setContainerSize( final ContainerId containerId, ContainerSize containerSize )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkNotNull( containerSize );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            localPeer.setContainerSize( containerId, containerSize );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }*/


    @Override
    public HostId getResourceHostIdByContainerId( final ContainerId containerId )
    {
        try
        {
            Preconditions.checkNotNull( containerId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId.getId() ) );

            return localPeer.getResourceHostIdByContainerId( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response getSshKeys( final EnvironmentId environmentId, final SshEncryptionType encryptionType )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );
            Preconditions.checkNotNull( encryptionType );

            final SshKeys keys = localPeer.getSshKeys( environmentId, encryptionType );
            return Response.ok( keys ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response createSshKey( final EnvironmentId environmentId, final SshEncryptionType encryptionType,
                                  final String containerId )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );
            Preconditions.checkNotNull( containerId );
            Preconditions.checkNotNull( encryptionType );

            final SshKey key = localPeer
                    .createSshKey( environmentId, JsonUtil.fromJson( containerId, ContainerId.class ), encryptionType );
            return Response.ok( key ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response getTemplateDownloadProgress( final EnvironmentId environmentId )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );

            PeerTemplatesDownloadProgress downloadProgress = localPeer.getTemplateDownloadProgress( environmentId );

            return Response.ok( downloadProgress ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public void updateEtcHostsWithNewContainerHostname( final EnvironmentId environmentId, final String oldHostname,
                                                        final String newHostname )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( oldHostname ) );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( newHostname ) );

            localPeer.updateEtcHostsWithNewContainerHostname( environmentId, oldHostname, newHostname );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public void updateAuthorizedKeysWithNewContainerHostname( final EnvironmentId environmentId,
                                                              final SshEncryptionType sshEncryptionType,
                                                              final String oldHostname, final String newHostname )
    {
        try
        {
            Preconditions.checkNotNull( environmentId );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( oldHostname ) );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( newHostname ) );
            Preconditions.checkNotNull( sshEncryptionType );

            localPeer.updateAuthorizedKeysWithNewContainerHostname( environmentId, oldHostname, newHostname,
                    sshEncryptionType );
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response addCustomProxy( final CustomProxyConfig proxyConfig )
    {
        try
        {
            Preconditions.checkNotNull( proxyConfig );

            localPeer.addCustomProxy( proxyConfig );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response removeCustomProxy( final CustomProxyConfig proxyConfig )
    {
        try
        {
            Preconditions.checkNotNull( proxyConfig );

            localPeer.removeCustomProxy( proxyConfig );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response excludePeerFromEnvironment( final String environmentId, final String peerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

            EnvironmentManager environmentManager = ServiceLocator.lookup( EnvironmentManager.class );

            environmentManager.excludePeerFromEnvironment( environmentId, peerId );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response excludeContainerFromEnvironment( final String environmentId, final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            EnvironmentManager environmentManager = ServiceLocator.lookup( EnvironmentManager.class );

            environmentManager.excludeContainerFromEnvironment( environmentId, containerId );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response updateContainerHostname( final String environmentId, final String containerId,
                                             final String hostname )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );

            EnvironmentManager environmentManager = ServiceLocator.lookup( EnvironmentManager.class );

            environmentManager.updateContainerHostname( environmentId, containerId, hostname );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }


    @Override
    public Response placeEnvironmentInfoByContainerId( final String environmentId, final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            EnvironmentManager environmentManager = ServiceLocator.lookup( EnvironmentManager.class );

            environmentManager.placeEnvironmentInfoByContainerId( environmentId, containerId );

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }
}
