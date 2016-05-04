package io.subutai.core.localpeer.impl.tasks;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.peer.ResourceHost;
import io.subutai.common.util.HostUtil;


public class ResetP2PSwarmSecretTask extends HostUtil.Task<Object>
{
    private final ResourceHost resourceHost;
    private final String p2pHash;
    private final String p2pSecretKey;
    private final long p2pSecretKeyTtl;


    public ResetP2PSwarmSecretTask( final ResourceHost resourceHost, final String p2pHash, final String p2pSecretKey,
                                    final long p2pSecretKeyTtl )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pSecretKey ) );
        Preconditions.checkArgument( p2pSecretKeyTtl > 0 );

        this.resourceHost = resourceHost;
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
        return String.format( "Reset p2p swarm %s secret", p2pHash );
    }


    @Override
    public Object call() throws Exception
    {
        resourceHost.resetSwarmSecretKey( p2pHash, p2pSecretKey, p2pSecretKeyTtl );

        return null;
    }
}
