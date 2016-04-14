package io.subutai.core.localpeer.impl.tasks;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.peer.ResourceHost;
import io.subutai.common.util.HostUtil;


public class JoinP2PSwarmTask extends HostUtil.Task<Object>
{
    private final ResourceHost resourceHost;
    private final String p2pIp;
    private final String p2pInterfaceName;
    private final String p2pHash;
    private final String p2pSecretKey;
    private final long p2pSecretKeyTtl;


    public JoinP2PSwarmTask( final ResourceHost resourceHost, final String p2pIp, final String p2pInterfaceName,
                             final String p2pHash, final String p2pSecretKey, final long p2pSecretKeyTtl )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pIp ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pInterfaceName ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pSecretKey ) );
        Preconditions.checkArgument( p2pSecretKeyTtl > 0 );

        this.resourceHost = resourceHost;
        this.p2pIp = p2pIp;
        this.p2pInterfaceName = p2pInterfaceName;
        this.p2pHash = p2pHash;
        this.p2pSecretKey = p2pSecretKey;
        this.p2pSecretKeyTtl = p2pSecretKeyTtl;
    }


    @Override
    public int maxParallelTasks()
    {
        return 0;
    }


    @Override
    public String name()
    {
        return String.format( "Join p2p swarm %s", p2pHash );
    }


    @Override
    public Object call() throws Exception
    {
        resourceHost.joinP2PSwarm( p2pIp, p2pInterfaceName, p2pHash, p2pSecretKey, p2pSecretKeyTtl );

        return null;
    }
}
