package org.safehaus.subutai.plugin.hbase.api.Cluster;


/**
 * Created by bahadyr on 11/14/14.
 */
public abstract class HBaseNode
{

    public abstract void start();

    public abstract void stop();

    public abstract void check();
}
