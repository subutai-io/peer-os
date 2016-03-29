package io.subutai.core.peer.impl;


import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.HostAddresses;
import io.subutai.common.environment.SshPublicKeys;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.security.WebClientBuilder;


/**
 * Environment REST client
 *
 * TODO throw exception if http code is not 2XX
 */
public class EnvironmentWebClient
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentWebClient.class );
    private Object provider;
    private RemotePeerImpl remotePeer;


    public EnvironmentWebClient( final Object provider, final RemotePeerImpl remotePeer )
    {
        this.provider = provider;
        this.remotePeer = remotePeer;
    }


    void startContainer( PeerInfo peerInfo, ContainerId containerId ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path =
                    String.format( "/%s/container/%s/start", containerId.getEnvironmentId().getId(), containerId.getId() );
            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );

            throw new PeerException( "Error starting container: " + e.getMessage() );
        }
    }


    //todo make consistent with other methods , use container id in path
    void stopContainer( PeerInfo peerInfo, ContainerId containerId ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/stop", containerId.getEnvironmentId().getId() );
            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.post( containerId );
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error stopping container:" + e.getMessage() );
        }
    }


    //todo make consistent with other methods , use container id in path
    public void destroyContainer( final PeerInfo peerInfo, final ContainerId containerId ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/destroy", containerId.getEnvironmentId().getId() );
            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.post( containerId );
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error destroying container: " + e.getMessage() );
        }
    }


    public ContainerHostState getState( final PeerInfo peerInfo, final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( containerId.getId() );

        try
        {
            remotePeer.checkRelation();
            String path =
                    String.format( "/%s/container/%s/state", containerId.getEnvironmentId().getId(), containerId.getId() );
            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider, 3000, 6000, 1 );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( ContainerHostState.class );
            }
        }
        catch ( Exception e )
        {
            LOG.warn( e.getMessage() );
            throw new PeerException( "Error on reading container state: " + e.getMessage() );
        }
    }


    public ProcessResourceUsage getProcessResourceUsage( final PeerInfo peerInfo, final ContainerId containerId,
                                                         int pid ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path =
                    String.format( "/%s/container/%s/usage/%d", containerId.getEnvironmentId().getId(), containerId.getId(),
                            pid );
            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            final Response response = client.get();
            if ( response.getStatus() == 500 )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( ProcessResourceUsage.class );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on obtaining process resource usage: " + e.getMessage() );
        }
    }


    public Set<Integer> getCpuSet( final PeerInfo peerInfo, final ContainerId containerId ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/%s/quota/cpuset", containerId.getEnvironmentId().getId(),
                    containerId.getId() );

            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            return new HashSet<>( client.getCollection( Integer.class ) );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on obtaining cpu set" + e.getMessage() );
        }
    }


    //todo add check for http response code
    public void setCpuSet( final PeerInfo peerInfo, final ContainerId containerId, final Set<Integer> cpuSet )
            throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/%s/quota/cpuset", containerId.getEnvironmentId().getId(),
                    containerId.getId() );

            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            client.post( cpuSet );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on setting cpu set: " + e.getMessage() );
        }
    }


    public ContainerQuota getAvailableQuota( final PeerInfo peerInfo, final ContainerId containerId )
            throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path = String.format( "/%s/container/%s/quota/available", containerId.getEnvironmentId().getId(),
                    containerId.getId() );

            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            return client.get( ContainerQuota.class );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on obtaining available quota: " + e.getMessage() );
        }
    }


    public ContainerQuota getQuota( final PeerInfo peerInfo, final ContainerId containerId ) throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path =
                    String.format( "/%s/container/%s/quota", containerId.getEnvironmentId().getId(), containerId.getId() );

            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            return client.get( ContainerQuota.class );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on obtaining available quota: " + e.getMessage() );
        }
    }


    //todo add check for http response code
    public void setQuota( final PeerInfo peerInfo, final ContainerId containerId, final ContainerQuota containerQuota )

            throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path =
                    String.format( "/%s/container/%s/quota", containerId.getEnvironmentId().getId(), containerId.getId() );

            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            client.post( containerQuota );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on setting quota: " + e.getMessage() );
        }
    }


    public HostId getResourceHostIdByContainerId( final PeerInfo peerInfo, final ContainerId containerId )
            throws PeerException
    {
        try
        {
            remotePeer.checkRelation();
            String path =
                    String.format( "/%s/container/%s/rhId", containerId.getEnvironmentId().getId(), containerId.getId() );
            WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );
            return client.get( HostId.class );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error on obtaining resource host id by container id", e );
        }
    }


    public SshPublicKeys generateSshKeysForEnvironment( final PeerInfo peerInfo, final EnvironmentId environmentId )
            throws PeerException
    {
        String path = String.format( "/%s/containers/sshkeys", environmentId.getId() );

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
            throw new PeerException( "Error generating ssh keys in environment", e );
        }

        if ( response.getStatus() == 500 )
        {
            throw new PeerException( response.readEntity( String.class ) );
        }

        return response.readEntity( SshPublicKeys.class );
    }


    public void configureSshInEnvironment( final PeerInfo peerInfo, final EnvironmentId environmentId,
                                           final SshPublicKeys sshPublicKeys ) throws PeerException
    {
        String path = String.format( "/%s/containers/sshkeys", environmentId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( peerInfo, path, provider );

        client.type( MediaType.APPLICATION_JSON );

        Response response;

        try
        {
            response = client.post( sshPublicKeys );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Error configuring ssh in environment", e );
        }

        if ( response.getStatus() == 500 )
        {
            throw new PeerException( response.readEntity( String.class ) );
        }
    }


    public void configureHostsInEnvironment( final PeerInfo peerInfo, final EnvironmentId environmentId,
                                             final HostAddresses hostAddresses ) throws PeerException
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
            throw new PeerException( "Error configuring hosts in environment", e );
        }

        if ( response.getStatus() == 500 )
        {
            throw new PeerException( response.readEntity( String.class ) );
        }
    }
}
