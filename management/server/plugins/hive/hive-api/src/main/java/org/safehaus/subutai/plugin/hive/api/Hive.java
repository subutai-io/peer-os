package org.safehaus.subutai.plugin.hive.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.ApiBase;


public interface Hive extends ApiBase<HiveConfig>
{

    public UUID installCluster( HiveConfig config, String hadoopClusterName );

    public UUID addNode( String hiveClusterName, String hostname );

    public UUID statusCheck( String hiveClusterName, String hostname );

    public UUID startNode( String hiveClusterName, String hostname );

    public UUID stopNode( String hiveClusterName, String hostname );

    public UUID restartNode( String hiveClusterName, String hostname );

    public UUID uninstallNode( String hiveClusterName, String hostname );

    public boolean isInstalled( String hadoopClusterName, String hostname );
}
