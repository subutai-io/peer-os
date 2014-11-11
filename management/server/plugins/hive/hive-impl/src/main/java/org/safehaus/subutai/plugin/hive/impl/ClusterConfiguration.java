package org.safehaus.subutai.plugin.hive.impl;


import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.ClusterConfigurationInterface;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;


public class ClusterConfiguration implements ClusterConfigurationInterface
{

    private HiveImpl manager;
    private TrackerOperation po;


    public ClusterConfiguration( final HiveImpl manager, final TrackerOperation po )
    {
        this.manager = manager;
        this.po = po;
    }


    public void configureCluster( final ConfigBase config, Environment environment )
            throws ClusterConfigurationException
    {
        HiveConfig hiveConfig = ( HiveConfig ) config;
        ContainerHost server = environment.getContainerHostByUUID( ( ( HiveConfig ) config ).getServer() );

        // configure hive server
        po.addLog( "Configuring server node: " + server.getHostname() );
        executeCommand( server, Commands.configureClient( server ) );


        for ( ContainerHost containerHost : environment.getContainers() )
        {
            if ( !containerHost.getId().equals( server.getId() ) )
            {
                po.addLog( "Configuring client node : " + containerHost.getHostname() );
                executeCommand( containerHost, Commands.configureClient( containerHost ) );
            }
        }
        hiveConfig.setEnvironmentId( environment.getId() );
        manager.getPluginDAO().saveInfo( HiveConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLogDone( HiveConfig.PRODUCT_KEY + " cluster data saved into database" );
    }


    public void executeCommand( ContainerHost host, String command )
    {
        try
        {
            host.execute( new RequestBuilder( command ) );
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
    }
}
