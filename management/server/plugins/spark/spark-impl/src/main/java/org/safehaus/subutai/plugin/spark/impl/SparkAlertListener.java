package org.safehaus.subutai.plugin.spark.impl;


import org.safehaus.subutai.core.metric.api.AlertListener;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.plugin.spark.api.Spark;


/**
 * Node resource threshold excess alert listener
 */
public class SparkAlertListener implements AlertListener
{

    public static final String SPARK_ALERT_LISTENER = "SPARK_ALERT_LISTENER";
    private Spark spark;


    public SparkAlertListener( final Spark spark )
    {
        this.spark = spark;
    }


    @Override
    public void onAlert( final ContainerHostMetric metric )
    {
        //TODO implement here cluster scaling functionality

        //check if source host belongs to spark cluster

        //if cluster has auto-scaling enabled:
        // obtain current quotas and figure out which one is exceeded
        // check if a quota limit increase does it:
        //   yes -> increase quota limit
        //   no -> add new node (here if all nodes of underlying Hadoop are already used, then notify user)
        //if auto-scaling disabled -> notify user
    }


    @Override
    public String getSubscriberId()
    {
        return SPARK_ALERT_LISTENER;
    }
}

