package io.subutai.core.peer.impl;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.HostAddresses;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.ReverseProxyConfig;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKey;
import io.subutai.common.security.SshKeys;
import io.subutai.common.security.WebClientBuilder;


/**
 * Environment REST client
 *
 * todo close web client
 */
public class EnvironmentWebClient
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentWebClient.class );
    private final Object provider;
    private final RemotePeerImpl remotePeer;
    private final PeerInfo peerInfo;


    public EnvironmentWebClient( final PeerInfo peerInfo, final Object provider, final RemotePeerImpl remotePeer )
    {
        Preconditions.checkNotNull( peerInfo );
        Preconditions.checkNotNull( provider );
        Preconditions.checkNotNull( remotePeer );

        this.peerInfo = peerInfo;
        this.provider = provider;
        this.remotePeer = remotePeer;
    }


    void startContainer( ContainerId containerId ) throws PeerException
    {
        Response response;
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/%s/start", containerId.getEnvironmentId().getId(),
                    containerId.getId() );
            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            response = client.post( null );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error starting container: " + e.getMessage() );
        }

        WebClientBuilder.checkResponse( response );
    }


    void stopContainer( ContainerId containerId ) throws PeerException
    {
        Response response;
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/%s/stop", containerId.getEnvironmentId().getId(),
                    containerId.getId() );
            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            response = client.post( null );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error stopping container:" + e.getMessage() );
        }

        WebClientBuilder.checkResponse( response );
    }


    public void destroyContainer( ContainerId containerId ) throws PeerException
    {
        Response response;
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/%s/destroy", containerId.getEnvironmentId().getId(),
                    containerId.getId() );
            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            response = client.post( null );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error destroying container: " + e.getMessage() );
        }

        WebClientBuilder.checkResponse( response );
    }


    public void setContainerHostName( final ContainerId containerId, final String hostname ) throws PeerException
    {
        Response response;
        try
        {
            remotePeer.checkRelation();

            String path = String.format( "/%s/container/%s/hostname", containerId.getEnvironmentId().getId(),
                    containerId.getId() );

            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            response = client.post( hostname );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error setting container hostname: " + e.getMessage() );
        }

        WebClientBuilder.checkResponse( response );
    }


    public ContainerHostState getState( ContainerId containerId ) throws PeerException
    {
        Response response;
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/%s/state", containerId.getEnvironmentId().getId(),
                    containerId.getId() );
            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider, 3000, 15000, 1 );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            response = client.get();
        }
        catch ( Exception e )
        {
            LOG.warn( e.getMessage() );
            throw new PeerException( "Error on reading container state: " + e.getMessage() );
        }

        return WebClientBuilder.checkResponse( response, ContainerHostState.class );
    }


    public ProcessResourceUsage getProcessResourceUsage( final ContainerId containerId, int pid ) throws PeerException
    {
        Response response;
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/%s/usage/%d", containerId.getEnvironmentId().getId(),
                    containerId.getId(), pid );
            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            response = client.get();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on obtaining process resource usage: " + e.getMessage() );
        }

        return WebClientBuilder.checkResponse( response, ProcessResourceUsage.class );
    }


    public ContainerQuota getQuota( final ContainerId containerId ) throws PeerException
    {
        Response response;
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/%s/quota", containerId.getEnvironmentId().getId(),
                    containerId.getId() );

            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            response = client.get();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on obtaining available quota: " + e.getMessage() );
        }

        return WebClientBuilder.checkResponse( response, ContainerQuota.class );
    }


    public void setQuota( final ContainerId containerId, final ContainerQuota containerQuota ) throws PeerException
    {
        Response response;
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/%s/quota", containerId.getEnvironmentId().getId(),
                    containerId.getId() );

            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            response = client.post( containerQuota );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on setting quota: " + e.getMessage() );
        }

        WebClientBuilder.checkResponse( response );
    }


    public HostId getResourceHostIdByContainerId( final ContainerId containerId ) throws PeerException
    {
        Response response;
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/%s/rhId", containerId.getEnvironmentId().getId(),
                    containerId.getId() );
            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            response = client.get();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on obtaining resource host id by container id: " + e.getMessage() );
        }

        return WebClientBuilder.checkResponse( response, HostId.class );
    }


    public SshKeys generateSshKeysForEnvironment( final EnvironmentId environmentId,
                                                  final SshEncryptionType sshKeyType ) throws PeerException
    {
        String path = String.format( "/%s/containers/sshkeys/%s", environmentId.getId(), sshKeyType );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

        client.accept( MediaType.APPLICATION_JSON );

        Response response;

        try
        {
            response = client.put( null );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error generating ssh keys in environment: " + e.getMessage() );
        }

        return WebClientBuilder.checkResponse( response, SshKeys.class );
    }


    public void configureSshInEnvironment( final EnvironmentId environmentId, final SshKeys sshKeys )
            throws PeerException
    {
        String path = String.format( "/%s/containers/sshkeys", environmentId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );

        Response response;

        try
        {
            response = client.post( sshKeys );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error configuring ssh in environment: " + e.getMessage() );
        }

        WebClientBuilder.checkResponse( response );
    }


    public void addSshKey( final EnvironmentId environmentId, final String sshPublicKey ) throws PeerException
    {
        String path = String.format( "/%s/containers/sshkey/add", environmentId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

        Response response;

        try
        {
            response = client.post( sshPublicKey );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error adding ssh key in environment: " + e.getMessage() );
        }

        WebClientBuilder.checkResponse( response );
    }


    public void removeSshKey( final EnvironmentId environmentId, final String sshPublicKey ) throws PeerException
    {
        String path = String.format( "/%s/containers/sshkey/remove", environmentId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

        Response response;

        try
        {
            response = client.post( sshPublicKey );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error removing ssh key in environment: " + e.getMessage() );
        }

        WebClientBuilder.checkResponse( response );
    }


    public void configureHostsInEnvironment( final EnvironmentId environmentId, final HostAddresses hostAddresses )
            throws PeerException
    {
        String path = String.format( "/%s/containers/hosts", environmentId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );

        Response response;

        try
        {
            response = client.post( hostAddresses );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error configuring hosts in environment: " + e.getMessage() );
        }

        WebClientBuilder.checkResponse( response );
    }


    public void addReverseProxy( final ReverseProxyConfig reverseProxyConfig ) throws PeerException
    {
        String path = String.format( "/%s/container/%s/reverseProxy", reverseProxyConfig.getEnvironmentId(),
                reverseProxyConfig.getContainerId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

        client.accept( MediaType.APPLICATION_JSON );
        client.type( MediaType.APPLICATION_JSON );
        Response response;

        try
        {
            response = client.post( reverseProxyConfig );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( String.format( "Error on adding reverse proxy: %s", e.getMessage() ) );
        }

        WebClientBuilder.checkResponse( response );
    }


    public SshKeys getSshKeys( final EnvironmentId environmentId, final SshEncryptionType sshEncryptionType )
            throws PeerException
    {
        String path = String.format( "/%s/sshkeys/%s", environmentId.getId(), sshEncryptionType );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

        Response response;

        try
        {
            response = client.get();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error reading ssh keys of the environment: " + e.getMessage() );
        }

        return WebClientBuilder.checkResponse( response, SshKeys.class );
    }


    public SshKey createSshKey( final EnvironmentId environmentId, final ContainerId containerId,
                                final SshEncryptionType sshEncryptionType ) throws PeerException
    {
        String path = String.format( "/%s/sshkeys/%s", environmentId.getId(), sshEncryptionType );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

        Response response;

        try
        {
            response = client.post( containerId );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error creating ssh key: " + e.getMessage() );
        }

        return WebClientBuilder.checkResponse( response, SshKey.class );
    }
}
