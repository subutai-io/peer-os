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


    public P2PCredentials( final String p2pHash, final String p2pSecretKey )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ), "Invalid p2p hash" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pSecretKey ), "Invalid p2p secret key" );

        this.p2pHash = p2pHash;
        this.p2pSecretKey = p2pSecretKey;
    }


    public String getP2pHash()
    {
        return p2pHash;
    }


    public String getP2pSecretKey()
    {
        return p2pSecretKey;
    }
}
