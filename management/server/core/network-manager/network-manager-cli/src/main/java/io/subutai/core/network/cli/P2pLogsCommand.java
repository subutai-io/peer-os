package io.subutai.core.network.cli;


import java.util.Date;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import com.google.common.base.Strings;

import io.subutai.common.network.JournalCtlLevel;
import io.subutai.common.network.P2pLogs;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "net", name = "p2p-logs" )
public class P2pLogsCommand extends SubutaiShellCommandSupport
{
    private final LocalPeer localPeer;

    @Argument( name = "host-id", description = "Host ID" )
    private String hostId;
    @Argument( index = 1, name = "minutes", description = "Last N min" )
    private int minutes = 30;


    public P2pLogsCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        ResourceHost resourceHost = Strings.isNullOrEmpty( hostId ) ? localPeer.getManagementHost() :
                                    localPeer.getResourceHostById( hostId );

        P2pLogs p2pLogs = resourceHost
                .getP2pLogs( JournalCtlLevel.ALL, new Date( System.currentTimeMillis() - minutes * 60 * 1000 ),
                        new Date() );

        for ( String log : p2pLogs.getLogs() )
        {
            System.out.println( log );
        }

        return null;
    }
}
