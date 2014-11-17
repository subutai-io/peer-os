package org.safehaus.subutai.plugin.hbase.impl;


import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;


public class Commands
{

    public final static String PACKAGE_NAME = Common.PACKAGE_PREFIX + HBaseConfig.PRODUCT_KEY.toLowerCase();


    public RequestBuilder getInstallDialogCommand()
    {

        return new RequestBuilder( "apt-get --assume-yes --force-yes install dialog" ).withTimeout( 360 )
                                                                                      .withStdOutRedirection(
                                                                                              OutputRedirection.NO );
    }


    public static RequestBuilder getInstallCommand()
    {

        return new RequestBuilder( "apt-get --assume-yes --force-yes install " + PACKAGE_NAME ).withTimeout( 360 )
                                                                                               .withStdOutRedirection(
                                                                                                       OutputRedirection.NO );
    }


    public RequestBuilder getUninstallCommand()
    {

        return new RequestBuilder( "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME ).withTimeout( 360 )
                                                                                             .withStdOutRedirection(
                                                                                                     OutputRedirection.NO );
    }


    public RequestBuilder getStartCommand()
    {
        return new RequestBuilder( "service hbase start &" );
    }


    public RequestBuilder getStopCommand()
    {
        return new RequestBuilder( "service hbase stop" ).withTimeout( 360 );
    }


    public static RequestBuilder getStatusCommand()
    {
        return new RequestBuilder( "service hbase status" );
    }


    public RequestBuilder getConfigBackupMastersCommand( String backUpMasters )
    {
        return new RequestBuilder( String.format( ". /etc/profile && backUpMasters.sh %s", backUpMasters ) );
    }


    public RequestBuilder getConfigQuorumCommand( String quorumPeers )
    {
        return new RequestBuilder( String.format( ". /etc/profile && quorum.sh %s", quorumPeers ) );
    }


    public RequestBuilder getConfigRegionCommand( String regionServers )
    {
        return new RequestBuilder( String.format( ". /etc/profile && region.sh %s", regionServers ) );
    }


    public RequestBuilder getConfigMasterCommand( String hadoopNameNodeHostname, String hMasterMachineHostname )
    {
        return new RequestBuilder(
                String.format( ". /etc/profile && master.sh %s %s", hadoopNameNodeHostname, hMasterMachineHostname ) );
    }


    public RequestBuilder getCheckInstalledCommand()
    {
        return new RequestBuilder( "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH );
    }
}
