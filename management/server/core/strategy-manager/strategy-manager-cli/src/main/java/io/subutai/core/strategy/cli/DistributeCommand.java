package io.subutai.core.strategy.cli;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.strategy.api.StrategyManager;


@Command( scope = "strategy", name = "distribute" )
public class DistributeCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger( DistributeCommand.class );
    private StrategyManager strategyManager;
    private PeerManager peerManager;
    private QuotaManager quotaManager;

    @Argument( index = 0, name = "strategyId", required = true, multiValued = false,
            description = "Identifier of distribution strategy" )
    private String strategyId;

    @Argument( index = 1, name = "peers", required = true, multiValued = true,
            description = "List of peers which are will participate on distribution." )
    private List<String> peers;


    public DistributeCommand( final StrategyManager strategyManager, final PeerManager peerManager,
                              final QuotaManager quotaManager )
    {
        this.strategyManager = strategyManager;
        this.peerManager = peerManager;
        this.quotaManager = quotaManager;
    }


    public void setStrategyId( final String strategyId )
    {
        this.strategyId = strategyId;
    }


    public void setPeers( final List<String> peers )
    {
        this.peers = peers;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        System.out.println("Not implemented");
        return null;
    }
}
