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
import io.subutai.common.quota.ContainerQuota;
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
            throw new PeerException( "Error starting container: " + e.getMessage() );
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
            throw new PeerException( "Error stopping container:" + e.getMessage() );
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
            throw new PeerException( "Error destroying container: " + e.getMessage() );
        }
    }


    public ContainerHostState getState( final String host, final ContainerId containerId ) throws PeerException
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
            throw new PeerException( "Error getting container state: " + e.getMessage() );
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
            throw new PeerException( "Error on obtaining process resource usage: " + e.getMessage() );
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
            throw new PeerException( "Error on obtaining cpu set" + e.getMessage() );
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
            throw new PeerException( "Error on setting cpu set: " + e.getMessage() );
        }
    }


    public ContainerQuota getAvailableQuota( final String host, final ContainerId containerId ) throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/available", containerId.getEnvironmentId().getId(),
                containerId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( ContainerQuota.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining available quota: " + e.getMessage() );
        }
    }


    public ContainerQuota getQuota( final String host, final ContainerId containerId ) throws PeerException
    {
        String path =
                String.format( "/%s/container/%s/quota", containerId.getEnvironmentId().getId(), containerId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( ContainerQuota.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining available quota: " + e.getMessage() );
        }
    }


    public void setQuota( final String host, final ContainerId containerId, final ContainerQuota containerQuota )

            throws PeerException
    {
        String path =
                String.format( "/%s/container/%s/quota", containerId.getEnvironmentId().getId(), containerId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( containerQuota );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on setting quota: " + e.getMessage() );
        }
    }
}
