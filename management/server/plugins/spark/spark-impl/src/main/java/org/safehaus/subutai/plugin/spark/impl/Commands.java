package org.safehaus.subutai.plugin.spark.impl;


import java.util.Set;

import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;


public class Commands
{

    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + SparkClusterConfig.PRODUCT_KEY.toLowerCase();


    public RequestBuilder getInstallCommand()
    {
        return new RequestBuilder( "apt-get --force-yes --assume-yes install " + PACKAGE_NAME ).withTimeout( 600 )
                                                                                               .withStdOutRedirection(
                                                                                                       OutputRedirection.NO );
    }


    public RequestBuilder getUninstallCommand()
    {
        return new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME ).withTimeout( 600 );
    }


    public RequestBuilder getCheckInstalledCommand()
    {
        return new RequestBuilder( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH );
    }


    public RequestBuilder getStartAllCommand()
    {
        return new RequestBuilder( "service spark-all start" ).withTimeout( 360 ).daemon();
    }


    public RequestBuilder getStopAllCommand()
    {
        return new RequestBuilder( "service spark-all stop" ).withTimeout( 60 );
    }


    public RequestBuilder getStatusAllCommand()
    {
        return new RequestBuilder( "service spark-all status" ).withTimeout( 60 );
    }


    public RequestBuilder getStartMasterCommand()
    {
        return new RequestBuilder( "service spark-master start" ).withTimeout( 90 );
    }


    public RequestBuilder getRestartMasterCommand()
    {
        return new RequestBuilder( "service spark-master stop && service spark-master start" ).withTimeout( 60 )
                                                                                              .daemon();
    }


    //    public RequestBuilder getRestartClusterCommand()
    //    {
    //        return new RequestBuilder( "service spark-all stop && service spark-all start" ).withTimeout( 60 );
    //    }


    public RequestBuilder getStopMasterCommand()
    {
        return new RequestBuilder( "service spark-master stop" ).withTimeout( 60 );
    }


    public RequestBuilder getStatusMasterCommand()
    {
        return new RequestBuilder( "service spark-master status" ).withTimeout( 60 );
    }


    public RequestBuilder getStartSlaveCommand()
    {
        return new RequestBuilder( "service spark-slave start" ).withTimeout( 90 );
    }


    public RequestBuilder getStatusSlaveCommand()
    {
        return new RequestBuilder( "service spark-slave status" ).withTimeout( 90 );
    }


    public RequestBuilder getStopSlaveCommand()
    {
        return new RequestBuilder( "service spark-slave stop" ).withTimeout( 60 );
    }


    public RequestBuilder getSetMasterIPCommand( String masterHostname )
    {
        return new RequestBuilder(
                String.format( ". /etc/profile && sparkMasterConf.sh clear ; sparkMasterConf.sh %s", masterHostname ) );
    }


    public RequestBuilder getClearSlavesCommand()
    {
        return new RequestBuilder( ". /etc/profile && sparkSlaveConf.sh clear" ).withTimeout( 60 );
    }


    public RequestBuilder getClearSlaveCommand( String slaveHostname )
    {
        return new RequestBuilder( String.format( ". /etc/profile && sparkSlaveConf.sh clear %s", slaveHostname ) )
                .withTimeout( 60 );
    }


    public RequestBuilder getAddSlaveCommand( String slaveHostname )
    {
        return new RequestBuilder( String.format( ". /etc/profile && sparkSlaveConf.sh %s", slaveHostname ) )
                .withTimeout( 60 );
    }


    public RequestBuilder getAddSlavesCommand( Set<String> slaveNodeHostnames )
    {
        StringBuilder slaves = new StringBuilder();
        for ( String slaveNode : slaveNodeHostnames )
        {
            slaves.append( slaveNode ).append( " " );
        }

        return new RequestBuilder(
                String.format( ". /etc/profile && sparkSlaveConf.sh clear ; sparkSlaveConf.sh %s", slaves ) )
                .withTimeout( 60 );
    }
}
