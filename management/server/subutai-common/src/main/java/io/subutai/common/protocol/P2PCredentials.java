package io.subutai.common.protocol;


import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class P2PCredentials
{
    @JsonProperty
    private String p2pHash;
    @JsonProperty
    private String p2pSecretKey;
    @JsonProperty
    private long p2pTtlSeconds;


    public P2PCredentials( final String p2pHash, final String p2pSecretKey, final long p2pTtlSeconds )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ), "Invalid p2p hash" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pSecretKey ), "Invalid p2p secret key" );
        Preconditions.checkArgument( p2pTtlSeconds > 0, "Invalid time-to-live" );

        this.p2pHash = p2pHash;
        this.p2pSecretKey = p2pSecretKey;
        this.p2pTtlSeconds = p2pTtlSeconds;
    }


    public String getP2pHash()
    {
        return p2pHash;
    }


    public String getP2pSecretKey()
    {
        return p2pSecretKey;
    }


    public long getP2pTtlSeconds()
    {
        return p2pTtlSeconds;
    }
}
