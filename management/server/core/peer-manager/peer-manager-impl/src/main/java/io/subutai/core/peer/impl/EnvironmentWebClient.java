package io.subutai.core.peer.impl;


import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;
import io.subutai.common.security.WebClientBuilder;


/**
 * Environment REST client
 */
public class EnvironmentWebClient
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentWebClient.class );
    private Object provider;


    public EnvironmentWebClient( final Object provider )
    {
        this.provider = provider;
    }


    void startContainer( String host, ContainerId containerId ) throws PeerException
    {
        String path =
                String.format( "/%s/container/%s/start", containerId.getEnvironmentId().getId(), containerId.getId() );
        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.get();
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error starting container", e );
        }
    }


    void stopContainer( String host, ContainerId containerId ) throws PeerException
    {
        String path = String.format( "/%s/container/stop", containerId.getEnvironmentId().getId() );
        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( containerId );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error stopping container", e );
        }
    }


    public void destroyContainer( final String host, final ContainerId containerId ) throws PeerException
    {
        String path = String.format( "/%s/container/destroy", containerId.getEnvironmentId().getId() );
        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( containerId );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error destroying container", e );
        }
    }


    public ContainerHostState getState( final String host, final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( containerId.getId() );

        String path =
                String.format( "/%s/container/%s/state", containerId.getEnvironmentId().getId(), containerId.getId() );
        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( ContainerHostState.class );
        }
        catch ( Exception e )
        {
            LOG.warn( "Error on getting container state: " + e.getMessage() );
            return ContainerHostState.UNKNOWN;
        }
    }


    public ProcessResourceUsage getProcessResourceUsage( final String host, final ContainerId containerId, int pid )
            throws PeerException
    {
        String path =
                String.format( "/%s/container/%s/usage/%d", containerId.getEnvironmentId().getId(), containerId.getId(),
                        pid );
        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( ProcessResourceUsage.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining process resource usage", e );
        }
    }


    public Set<Integer> getCpuSet( final String host, final ContainerId containerId ) throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/cpuset", containerId.getEnvironmentId().getId(),
                containerId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return new HashSet<>( client.getCollection( Integer.class ) );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining cpu set", e );
        }
    }


    public void setCpuSet( final String host, final ContainerId containerId, final Set<Integer> cpuSet )
            throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/cpuset", containerId.getEnvironmentId().getId(),
                containerId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( cpuSet );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on setting cpu set", e );
        }
    }


    public ResourceValue getAvailableQuota( final String host, final ContainerId containerId,
                                            final ResourceType resourceType ) throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/%s/available", containerId.getEnvironmentId().getId(),
                containerId.getId(), resourceType );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( ResourceValue.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining available quota", e );
        }
    }


    public ResourceValue getQuota( final String host, final ContainerId containerId, final ResourceType resourceType )
            throws PeerException
    {
        String path =
                String.format( "/%s/container/%s/quota/%s", containerId.getEnvironmentId().getId(), containerId.getId(),
                        resourceType );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( ResourceValue.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining available quota", e );
        }
    }


    public void setQuota( final String host, final ContainerId containerId, final ResourceType resourceType,
                          ResourceValue resourceValue )

            throws PeerException
    {
        String path =
                String.format( "/%s/container/%s/quota/%s", containerId.getEnvironmentId().getId(), containerId.getId(),
                        resourceType );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( resourceValue );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on setting quota", e );
        }
    }
}
