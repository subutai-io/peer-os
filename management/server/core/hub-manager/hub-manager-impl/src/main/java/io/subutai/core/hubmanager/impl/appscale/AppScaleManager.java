package io.subutai.core.hubmanager.impl.appscale;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
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
        log.debug( "AppScale installation started" );

        Preconditions.checkArgument( config != null, "Null config" );

        Preconditions.checkArgument( !StringUtils.isEmpty( config.getUserDomain() ), "User domain is null" );

        ContainerHost controllerHost = getContainerHost( config.getClusterName() );

        execute( controllerHost, Commands.getCreateLogDir() );

        appscaleInitCluster( controllerHost, config );

        makeCleanUpPreviousInstallation( controllerHost );

        runAfterInitCommands( controllerHost, config );

        addKeyPairSH( controllerHost );

        createRunSH ( controllerHost );

        int envContainersCount = config.getContainerAddresses().size();

        String runShell = Commands.getRunShell() + " " + envContainersCount;

        execute( controllerHost, runShell );

        createUpShell ( controllerHost );

        runInRH ( controllerHost, config );

        execute( controllerHost, "echo '127.0.1.1 appscale-image0' >> /etc/hosts" );

        execute( controllerHost, Commands.backUpSSH () );

        execute( controllerHost, Commands.backUpAppscale () );

        log.debug( "AppScale installation done" );
    }


    private void runInRH ( ContainerHost containerHost, AppScaleConfigDto config )
    {
        String ipAddress = config.getContainerAddresses().get( config.getClusterName() );

        try
        {
            ResourceHost resourceHostByContainerId = localPeer.getResourceHostByContainerId( containerHost.getId() );

            CommandResult resultStr = resourceHostByContainerId
                    .execute ( new RequestBuilder ( "grep vlan /mnt/lib/lxc/" + config.getClusterName() + "/config" ) );

            String stdOut = resultStr.getStdOut ();

            String vlanString = stdOut.substring ( 11, 14 );

            resourceHostByContainerId.execute ( new RequestBuilder ( "subutai proxy del " + vlanString + " -d" ) );

            resourceHostByContainerId.execute ( new RequestBuilder (
                    "subutai proxy add " + vlanString + " -d \"*." + config.getUserDomain () + "\" -f /mnt/lib/lxc/"
                    + config.getClusterName() + "/rootfs/etc/nginx/ssl.pem" ) );

            resourceHostByContainerId
                    .execute ( new RequestBuilder ( "subutai proxy add " + vlanString + " -h " + ipAddress ) );
        }
        catch ( Exception e )
        {
            log.error( "Error to set proxy settings: ", e );
        }
    }


    private void createUpShell ( ContainerHost containerHost )
    {

        String a = "#!/usr/bin/expect -f\n" + "set timeout -1\n" + "set num $argv\n"
                + "spawn /root/appscale-tools/bin/appscale up\n" + "\n"
                + "for {set i 1} {\"$i\" <= \"$num\"} {incr i} {\n"
                + "expect \"Enter your desired admin e-mail address:\"\n" + "send -- \"a@a.com\\n\"\n"
                + "expect \"Enter new password:\"\n" + "send -- \"aaaaaa\\n\"\n" + "expect \"Confirm password:\"\n"
                + "send -- \"aaaaaa\\n\"\n" + "\n" + "}\n" + "\n" + "expect EOD";

        try
        {
            containerHost.execute ( new RequestBuilder ( "touch /root/up.sh" ) );
            containerHost.execute ( new RequestBuilder ( "echo '" + a + "' > /root/up.sh" ) );
            containerHost.execute ( new RequestBuilder ( "chmod +x /root/up.sh" ) );
        }
        catch ( CommandException ex )
        {
            log.error ( "Error on create up shell: " + ex );
        }
    }


    private void createRunSH( ContainerHost containerHost )
    {

        try
        {
            containerHost.execute ( new RequestBuilder ( "rm /root/run.sh " ) );
            containerHost.execute ( new RequestBuilder ( "touch /root/run.sh" ) );
            containerHost.execute ( new RequestBuilder ( "echo '" + returnRunSH() + "' > /root/run.sh" ) );
            containerHost.execute ( new RequestBuilder ( "chmod +x /root/run.sh" ) );
        }
        catch ( CommandException ex )
        {
            log.error ( "createRunSSH error" + ex );
        }
    }


    private String returnRunSH ()
    {
        String sh = "#!/usr/bin/expect -f\n" + "set timeout -1\n" + "set num $argv\n"
                + "spawn /root/appscale-tools/bin/appscale up\n" + "\n"
//                + "for {set i 1} {\"$i\" <= \"$num\"} {incr i} {\n"
//                + "    expect \"Are you sure you want to continue connecting (yes/no)?\"\n" + "    send -- \"yes\\n\"\n"
//                + "    expect \" password:\"\n" + "    send -- \"a\\n\"\n" + "}\n" + "\n"
                + "expect \"Enter your desired admin e-mail address:\"\n" + "send -- \"a@a.com\\n\"\n"
                + "expect \"Enter new password:\"\n" + "send -- \"aaaaaa\\n\"\n" + "expect \"Confirm password:\"\n"
                + "send -- \"aaaaaa\\n\"\n" + "\n" + "expect EOD";

        return sh;
    }


    private void addKeyPairSH( ContainerHost containerHost )
    {
        try
        {
            String add = "#!/usr/bin/expect -f\n"
                    + "set timeout -1\n"
                    + "set num argv\n"
                    + "spawn /root/appscale-tools/bin/appscale-add-keypair --ips ips.yaml --keyname appscale\n"
                    + "for {set i 1} {\"$i\" <= \"$num\"} {incr i} {\n"
                    + "    expect \"Are you sure you want to continue connecting (yes/no)?\"\n"
                    + "    send -- \"yes\\n\"\n"
                    + "    expect \" password:\"\n"
                    + "    send -- \"a\\n\"\n"
                    + "}\n"
                    + "expect EOD";

            containerHost.execute( new RequestBuilder ( "mkdir .ssh" ) );
            containerHost.execute( new RequestBuilder ( "rm /root/addKey.sh " ) );
            containerHost.execute( new RequestBuilder ( "touch /root/addKey.sh" ) );
            containerHost.execute( new RequestBuilder ( "echo '" + add + "' > /root/addKey.sh" ) );
            containerHost.execute( new RequestBuilder ( "chmod +x /root/addKey.sh" ) );
        }
        catch ( CommandException ex )
        {
            log.error ( ex.toString () );
        }
    }


    private void runAfterInitCommands( ContainerHost containerHost, AppScaleConfigDto config )
    {
        execute( containerHost, "sed -i 's/{0}:{1}/{1}.{0}/g' /root/appscale/AppDashboard/lib/app_dashboard_data.py" );

        execute( containerHost, "echo -e '127.0.0.1 " + config.getUserDomain () + "' >> /etc/hosts" );

        execute( containerHost,
                "sed -i 's/127.0.0.1 localhost.localdomain localhost/127.0.0.1 localhost.localdomain localhost "
                + config.getUserDomain () + "/g' " + "/root/appscale/AppController/djinn.rb" );

        execute( containerHost,
                "sed -i 's/127.0.0.1 localhost.localdomain localhost/127.0.0.1 localhost.localdomain localhost "
                + config.getUserDomain () + "/g' " + "/etc/hosts" );

        execute( containerHost, "cat /etc/nginx/mykey.pem /etc/nginx/mycert.pem > /etc/nginx/ssl.pem" );

        //
        // Modify navigation.html
        //

        String addButton = "<li align=\"center\" class=\"tab\"><a class=\"btn btn-info\" href=\"{{ flower_url }}\">TaskQueue Monitor<\\/a><\\/li>";

        String replaceString = addButton + "<br><li align=\"center\" class=\"tab\"><a class=\"btn btn-info\" href=\"#\"  onClick=\"growEnvironment()\">Add Appengine<\\/a><\\/li>";

        execute( containerHost,
                "sed -i 's/ " + addButton + "/" + replaceString + "/g' /root/appscale/AppDashboard/templates/shared/navigation.html" );

        String changeMonitURL = "sed -i 's/{{ monit_url }}/http:\\/\\/2812." + config.getUserDomain () + "/g' /root/appscale/AppDashboard/templates/shared/navigation.html";

        execute( containerHost, changeMonitURL );

        String changeFlowerURL = "sed -i 's/{{ flower_url }}/http:\\/\\/5555." + config.getUserDomain () + "/g' /root/appscale/AppDashboard/templates/shared/navigation.html";

        execute( containerHost, changeFlowerURL );

        String modUrl = "resturl?";

        String modUrlChange = "1443." + config.getUserDomain () + "\\/rest\\/appscale\\/growenvironment?containerName="
                + config.getClusterName () + "&";

        execute( containerHost,
                "sed -i 's/" + modUrl + "/" + modUrlChange + "/g' /root/appscale/AppDashboard/templates/shared/navigation.html" );

        //
        // Modify ss_agent.py
        //

        String modstr = "thispathtochange = \"\\/rest\\/appscale\\/growenvironment?clusterName=\\\"";

        String modstrchange = "thispathtochange = \"\\/rest\\/appscale\\/growenvironment?clusterName=\"" + config.getClusterName ();

        execute( containerHost,
                "sed -i 's/" + modstr + "/" + modstrchange + "/g' /root/appscale/InfrastructureManager/agents/ss_agent.py" );

        String tokenUrl = "subutai:8443";

        String tokenUrlChange = "1443." + config.getUserDomain ();

        execute( containerHost,
                "sed -i 's/" + tokenUrl + "/" + tokenUrlChange + "/g' /root/appscale/InfrastructureManager/agents/ss_agent.py" );

        //
        // Modify nginx
        //

        String nginx
                = "echo 'server {\n" + "        listen        80;\n" + "        server_name   ~^(?<port>.+)\\." + config
                .getUserDomain () + "$;\n" + "\n" + "    set $appbackend \"127.0.0.1:${port}\";\n" + "\n"
                + "    # proxy to AppScale over http\n" + "    if ($port = 1443) {\n"
                + "        set $appbackend \"appscaledashboard\";\n" + "    }\n" + "\n" + "    location / {\n"
                + "        proxy_pass http://$appbackend;\n"
                + "        proxy_set_header   X-Real-IP $remote_addr;\n"
                + "        proxy_set_header   Host $http_host;\n"
                + "        proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;\n" + "\n" + "    }\n"
                + "}' > /etc/nginx/sites-enabled/default";

        execute( containerHost, nginx );
    }

    
    private void makeCleanUpPreviousInstallation( ContainerHost containerHost )
    {
        try
        {
            CommandResult cr = containerHost.execute ( new RequestBuilder ( "ls /root/.ssh" ) );

            if ( !cr.toString ().equals ( "" ) )
            {
//                execute( containerHost, "rm /root/.ssh/*" );
                execute( containerHost, "touch /root/.ssh/known_hosts" );
            }

            cr = containerHost.execute ( new RequestBuilder ( "ls /root/.appscale" ) );

            if ( !cr.toString ().equals ( "" ) )
            {
                execute( containerHost, "rm /root/.appscale/*" );
            }
        }
        catch ( CommandException e )
        {
            log.error ( "Clean process command exception: ", e );
        }
    }


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
