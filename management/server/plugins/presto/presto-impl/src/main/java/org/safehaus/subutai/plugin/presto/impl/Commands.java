package org.safehaus.subutai.plugin.presto.impl;


import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.command.OutputRedirection;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;


public class Commands
{

    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + PrestoClusterConfig.PRODUCT_KEY.toLowerCase();


    public RequestBuilder getInstallCommand()
    {
        RequestBuilder rb =
                new RequestBuilder( "apt-get --force-yes --assume-yes install " + PACKAGE_NAME ).withTimeout( 600 )
                                                                                                .withStdOutRedirection(
                                                                                                        OutputRedirection.NO );
        return rb;
    }


    public RequestBuilder getUninstallCommand()
    {
        RequestBuilder rb =
                new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME ).withTimeout( 600 );
        return rb;
    }


    public RequestBuilder getCheckInstalledCommand()
    {
        return new RequestBuilder( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH );
    }


    public RequestBuilder getStartCommand()
    {
        return new RequestBuilder( "service presto start" ).withTimeout( 60 );
    }


    public RequestBuilder getStopCommand()
    {
        return new RequestBuilder( "service presto stop" ).withTimeout( 60 );
    }


    public RequestBuilder getRestartCommand()
    {
        return new RequestBuilder( "service presto restart" ).withTimeout( 60 );
    }


    public RequestBuilder getStatusCommand()
    {
        return new RequestBuilder( "service presto status" );
    }


    public RequestBuilder getSetCoordinatorCommand( ContainerHost coordinatorNode )
    {
        String s = String.format( "presto-config.sh coordinator %s", coordinatorNode.getHostname() );
        return new RequestBuilder( s ).withTimeout( 60 );
    }


    public RequestBuilder getSetWorkerCommand( ContainerHost node )
    {
        String s = String.format( "presto-config.sh worker %s", node.getHostname() );
        return new RequestBuilder( s ).withTimeout( 60 );
    }
}
