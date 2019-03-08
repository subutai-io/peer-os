package io.subutai.common.protocol;


import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class P2PCredentials
{
    @JsonProperty
    private String environmentId;
    @JsonProperty
    private String p2pHash;
    @JsonProperty
    private String p2pSecretKey;
    @JsonProperty
    private long p2pTtlSeconds;


    public P2PCredentials( @JsonProperty( "environmentId" ) final String environmentId,
                           @JsonProperty( "p2pHash" ) final String p2pHash,
                           @JsonProperty( "p2pSecretKey" ) final String p2pSecretKey,
                           @JsonProperty( "p2pTtlSeconds" ) final long p2pTtlSeconds )
    {
        Preconditions.checkArgument( !StringUtils.isBlank( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( !StringUtils.isBlank( p2pHash ), "Invalid p2p hash" );
        Preconditions.checkArgument( !StringUtils.isBlank( p2pSecretKey ), "Invalid p2p secret key" );
        Preconditions.checkArgument( p2pTtlSeconds > 0, "Invalid time-to-live" );

        this.environmentId = environmentId;
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


    public String getEnvironmentId()
    {
        return environmentId;
    }
}
