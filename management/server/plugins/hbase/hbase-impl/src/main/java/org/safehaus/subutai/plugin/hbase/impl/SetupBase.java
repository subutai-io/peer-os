package org.safehaus.subutai.plugin.hbase.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;

import com.google.common.base.Preconditions;


public class SetupBase
{

    final TrackerOperation po;
    final HBaseImpl manager;
    final HBaseConfig config;


    public SetupBase( TrackerOperation po, HBaseImpl manager, HBaseConfig config )
    {

        Preconditions.checkNotNull( config, "HBase cluster config is null" );
        Preconditions.checkNotNull( po, "HBase operation tracker is null" );
        Preconditions.checkNotNull( manager, "HBase manager is null" );

        this.po = po;
        this.manager = manager;
        this.config = config;
    }


    void checkConnected() throws ClusterSetupException
    {

        /*String hostname = config.getHbaseMaster().getHostname();
        if ( manager.getAgentManager().getAgentByHostname( hostname ) == null )
        {
            throw new ClusterSetupException( "Master node is not connected" );
        }

        for ( UUID a : config.getRegionServers() )
        {
            if ( manager.getAgentManager().getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Not all region server nodes are connected" );
            }
        }

        for ( UUID a : config.getQuorumPeers() )
        {
            if ( manager.getAgentManager().getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Not all quorum peer nodes are connected" );
            }
        }


        for ( UUID a : config.getBackupMasters() )
        {
            if ( manager.getAgentManager().getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Not all backup master nodes are connected" );
            }
        }*/
    }
}

