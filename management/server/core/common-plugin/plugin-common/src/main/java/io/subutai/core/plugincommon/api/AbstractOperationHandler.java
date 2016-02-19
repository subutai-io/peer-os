package io.subutai.core.plugincommon.api;


import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.tracker.TrackerOperation;


public abstract class AbstractOperationHandler<T extends ApiBase<V>, V extends ConfigBase> implements Runnable
{
    protected final T manager;
    protected final String clusterName;
    protected TrackerOperation trackerOperation;
    protected final V config;


    public AbstractOperationHandler( T manager, String clusterName )
    {
        Preconditions.checkNotNull( manager, "Manager is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( clusterName ), "Cluster name is null or empty" );
        this.manager = manager;
        this.clusterName = clusterName;
        this.config = manager.getCluster( clusterName );
    }


    public AbstractOperationHandler( T manager, V config )
    {
        Preconditions.checkNotNull( manager, "Manager is null" );
        Preconditions.checkNotNull( config, "Configuration is null" );
        this.manager = manager;
        this.config = config;
        this.clusterName = config.getClusterName();
    }


    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    public String getClusterName()
    {
        return clusterName;
    }


    public TrackerOperation getTrackerOperation()
    {
        return trackerOperation;
    }


    public V getConfig()
    {
        return config;
    }


    public T getManager()
    {
        return manager;
    }
}
