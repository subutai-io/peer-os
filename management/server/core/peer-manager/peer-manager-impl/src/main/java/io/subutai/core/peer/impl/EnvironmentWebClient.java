package io.subutai.core.peer.impl;


import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.quota.CpuQuota;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.RamQuota;
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
        String path = String.format( "/%s/container/state", containerId.getEnvironmentId().getId() );
        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.post( containerId, ContainerHostState.class );
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


    public RamQuota getRamQuota( final String host, final ContainerId containerId ) throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/ram", containerId.getEnvironmentId().getId(),
                containerId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( RamQuota.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining ram quota", e );
        }
    }


    public void setRamQuota( final String host, final ContainerId containerId, final RamQuota ramQuota )
            throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/ram", containerId.getEnvironmentId().getId(),
                containerId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( ramQuota );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on setting ram quota", e );
        }
    }


    public CpuQuota getCpuQuota( final String host, final ContainerId containerId ) throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/cpu", containerId.getEnvironmentId().getId(),
                containerId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( CpuQuota.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining cpu quota", e );
        }
    }


    public void setCpuQuota( final String host, final ContainerId containerId, final CpuQuota cpuQuota )
            throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/cpu", containerId.getEnvironmentId().getId(),
                containerId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( cpuQuota );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on setting cpu quota", e );
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


    public DiskQuota getDiskQuota( final String host, final ContainerId containerId, final DiskPartition diskPartition )
            throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/disk/%s", containerId.getEnvironmentId().getId(),
                containerId.getId(), diskPartition );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( DiskQuota.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining cpu set", e );
        }
    }


    public void setDiskQuota( final String host, final ContainerId containerId, final DiskQuota diskQuota )
            throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/disk", containerId.getEnvironmentId().getId(),
                containerId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            client.post( diskQuota );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on setting disk quota", e );
        }
    }


    public RamQuota getAvailableRamQuota( final String host, final ContainerId containerId ) throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/ram/available", containerId.getEnvironmentId().getId(),
                containerId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( RamQuota.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining available ram quota", e );
        }
    }


    public CpuQuota getAvailableCpuQuota( final String host, final ContainerId containerId ) throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/cpu/available", containerId.getEnvironmentId().getId(),
                containerId.getId() );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( CpuQuota.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining available cpu quota", e );
        }
    }


    public DiskQuota getAvailableDiskQuota( final String host, final ContainerId containerId,
                                            final DiskPartition diskPartition ) throws PeerException
    {
        String path = String.format( "/%s/container/%s/quota/disk/%s/available", containerId.getEnvironmentId().getId(),
                containerId.getId(), diskPartition );

        WebClient client = WebClientBuilder.buildEnvironmentWebClient( host, path, provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );
        try
        {
            return client.get( DiskQuota.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on obtaining available disk quota", e );
        }
    }
}
