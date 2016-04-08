package io.subutai.core.hubmanager.impl.appscale;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.AppScaleConfigDto;

import static java.lang.String.format;


/**
 * Dirty copy from io.subutai.plugin.appscale.impl.ClusterConfiguration.
 * Should be refactored.
 */
public class AppScaleManager
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final LocalPeer localPeer;


    public AppScaleManager( PeerManager peerManager )
    {
        this.localPeer = peerManager.getLocalPeer();
    }


    void installCluster( AppScaleConfigDto config )
    {
        Preconditions.checkArgument( config != null, "Null config" );
        Preconditions.checkArgument( !StringUtils.isEmpty( config.getUserDomain() ), "Domain is null" );

        String controllerContainerName = config.getClusterName();

        log.debug( "controllerContainerName: {}", controllerContainerName );

        ContainerHost controllerHost = getContainerHost( controllerContainerName );

//        execute( controllerHost, "echo '" + token + "' > /token" );

        execute( controllerHost, Commands.getCreateLogDir() );

        appscaleInitCluster ( controllerHost, config );

//        makeCleanUpPreviousInstallation ( containerHost );

//        this.runAfterInitCommands ( containerHost, config );
//        this.addKeyPairSH ( containerHost );
//
//        LOG.info ( "Run shell starting..." );
//        this.createRunSH ( containerHost ); // we only need this in master container...
//
//        String runShell = Commands.getRunShell ();
//        runShell = runShell + " " + numberOfContainers;
//        LOG.info ( "RUN SHELL COMMAND: " + runShell );
//
//        try
//        {
//            containerHost.execute( new RequestBuilder( runShell ).withTimeout( 10000 ) );
//        }
//        catch ( CommandException ex )
//        {
//            LOG.error ( "RUN SHELL ERROR" + ex );
//        }
//        LOG.info ( "Run shell completed..." );
//        this.createUpShell ( containerHost );
//        LOG.info ( "RH command started" );
//
//        this.runInRH ( containerHost, config.getClusterName (), config );
//        LOG.info ( "RH command ended" );
//
//        LOG.info ( "Environment ID: " + environment.getId () );
//
//        config.setEnvironmentId ( environment.getId () );
//        boolean saveInfo = appscaleManager.getPluginDAO ()
//                                          .saveInfo ( AppScaleConfig.PRODUCT_KEY, configBase.getClusterName (),
//                                                  configBase );
//        try
//        {
//            appscaleManager.getEnvironmentManager ()
//                           .startMonitoring ( AppscaleAlertHandler.HANDLER_ID, AlertHandlerPriority.NORMAL,
//                                   environment.getId () );
//            po.addLog ( "Alert handler added successfully." );
//        }
//        catch ( EnvironmentManagerException e )
//        {
//            LOG.error ( e.getMessage (), e );
//            po.addLogFailed ( "Could not add alert handler to monitor this environment." );
//        }
//        LOG.info ( "SAVE INFO: " + saveInfo );
//        LOG.info ( "Appscale saved to database" );
//
//        String containerIP = this.getIPAddress ( containerHost );
//
//        this.commandExecute ( containerHost, "echo '127.0.1.1 appscale-image0' >> /etc/hosts" );
//        this.commandExecute ( containerHost, Commands.backUpSSH () );
//        this.commandExecute ( containerHost, Commands.backUpAppscale () );
    }


    //
    private void appscaleInitCluster ( ContainerHost containerHost, AppScaleConfigDto config )
    {
        execute( containerHost, "rm -f /root/AppScalefile && touch /root/AppScalefile" );

        execute( containerHost, "echo ips_layout: >> /root/AppScalefile" );

        //
        // Insert master IP
        //

        Map<String, String> ip = config.getContainerAddresses();

        String masterIP = ip.get( config.getClusterName() );

        execute( containerHost, format( "echo '  master : %s' >> /root/AppScalefile", masterIP ) );

        //
        // Insert AppEngine IPs
        //

        execute( containerHost, "echo '  appengine:' >> /root/AppScalefile" );

        for ( String hostname : config.getAppenList() )
        {
            execute( containerHost, format( "echo '  - %s' >> /root/AppScalefile", ip.get( hostname ) ) );
        }

        //
        // Insert Zookeeper IPs
        //

        execute( containerHost, "echo '  zookeeper:' >> /root/AppScalefile" );

        for ( String hostname : config.getZooList() )
        {
            execute( containerHost, format( "echo '  - %s' >> /root/AppScalefile", ip.get( hostname ) ) );
        }

        //
        // Insert Cassandra IPs
        //

        execute( containerHost, "echo '  database:' >> /root/AppScalefile" );

        for ( String hostname : config.getCassList() )
        {
            execute( containerHost, format( "echo '  - %s' >> /root/AppScalefile", ip.get( hostname ) ) );
        }

        execute( containerHost, format( "echo login: %s >> /root/AppScalefile", masterIP ) );

        execute( containerHost, "echo 'force: True' >> /root/AppScalefile" );

        execute( containerHost, "cp /root/AppScalefile /" );
    }


    private ContainerHost getContainerHost( String hostname )
    {
        ContainerHost ch = null;

        try
        {
            ch = localPeer.getContainerHostByName( hostname );
        }
        catch ( HostNotFoundException e )
        {
            log.error( "Error to get container by name: ", e );
        }

        return ch;
    }


    private void execute( ContainerHost ch, String command )
    {
        try
        {
            ch.execute( new RequestBuilder ( command ).withTimeout( 10000 ) );
        }
        catch ( CommandException e )
        {
            log.error ( "Error to execute command: ", e );
        }
    }

}
