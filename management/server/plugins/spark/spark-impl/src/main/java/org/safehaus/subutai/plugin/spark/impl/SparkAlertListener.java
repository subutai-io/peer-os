package org.safehaus.subutai.plugin.spark.impl;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.metric.api.AlertListener;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Node resource threshold excess alert listener
 */
public class SparkAlertListener implements AlertListener
{
    private static final Logger LOG = LoggerFactory.getLogger( SparkAlertListener.class.getName() );

    public static final String SPARK_ALERT_LISTENER = "SPARK_ALERT_LISTENER";
    private SparkImpl spark;


    public SparkAlertListener( final SparkImpl spark )
    {
        this.spark = spark;
    }


    @Override
    public void onAlert( final ContainerHostMetric metric )
    {
        //TODO implement here cluster scaling functionality

        //find spark cluster by environment id
        List<SparkClusterConfig> clusters = spark.getClusters();

        SparkClusterConfig targetCluster = null;
        for ( SparkClusterConfig cluster : clusters )
        {
            if ( cluster.getEnvironmentId().equals( metric.getEnvironmentId() ) )
            {
                targetCluster = cluster;
                break;
            }
        }

        if ( targetCluster == null )
        {
            LOG.warn( String.format( "Cluster not found by environment id %s", metric.getEnvironmentId() ) );
            return;
        }

        //get cluster environment
        Environment environment = spark.getEnvironmentManager().getEnvironmentByUUID( metric.getEnvironmentId() );
        if ( environment == null )
        {
            LOG.warn( String.format( "Environment not found by id %s", metric.getEnvironmentId() ) );
            return;
        }

        //get environment containers and find alert's source host
        Set<ContainerHost> containers = environment.getContainerHosts();

        ContainerHost sourceHost = null;
        for ( ContainerHost containerHost : containers )
        {
            if ( containerHost.getHostname().equalsIgnoreCase( metric.getHost() ) )
            {
                sourceHost = containerHost;
            }
        }
        if ( sourceHost == null )
        {
            LOG.warn( String.format( "Alert source host %s not found in environment", metric.getHost() ) );
            return;
        }
        //check if source host belongs to found spark cluster
        if ( !targetCluster.getAllNodesIds().contains( sourceHost.getId() ) )
        {
            LOG.warn( String.format( "Alert source host %s does not belong to Spark cluster", metric.getHost() ) );
            return;
        }

        //if cluster has auto-scaling enabled:

        if ( targetCluster.isAutoScaling() )
        {
            // obtain current quotas and figure out which one is exceeded
            // check if a quota limit increase does it:
            //   yes -> increase quota limit
            //   no -> add new node (here if all nodes of underlying Hadoop are already used, then notify user)
        }
        else
        {
            //if auto-scaling disabled -> notify user
        }
    }


    @Override
    public String getSubscriberId()
    {
        return SPARK_ALERT_LISTENER;
    }
}

