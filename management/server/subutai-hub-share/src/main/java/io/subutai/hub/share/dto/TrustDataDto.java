package io.subutai.hub.share.dto;


import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


public class TrustDataDto
{
    public enum TrustLevel
    {
        ULTIMATE, FULL, MARGINAL, NEVER
    }

    private String keyId;

    private String principalKeyId;

    private TrustLevel trustLevel;


    public TrustDataDto()
    {
    }


    public TrustDataDto( String keyId, String principalKeyId, TrustLevel trustLevel )
    {
        this.keyId = keyId;
        this.principalKeyId = principalKeyId;
        this.trustLevel = trustLevel;
    }


    public String getKeyId()
    {
        return keyId;
    }


    public String getPrincipalKeyId()
    {
        return principalKeyId;
    }


    public TrustLevel getTrustLevel()
    {
        return trustLevel;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( !( obj instanceof TrustDataDto ) )
        {
            return false;
        }

        TrustDataDto another = ( TrustDataDto ) obj;

        return Objects.equals( keyId, another.keyId ) &&
                Objects.equals( principalKeyId, another.principalKeyId ) &&
                Objects.equals( trustLevel, another.trustLevel );
    }


    @Override
    public int hashCode()
    {
        return Objects.hash( keyId, principalKeyId, trustLevel );
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE )
                .append( "keyId", keyId )
                .append( "principalKeyId", principalKeyId )
                .append( "trustLevel", trustLevel )
                .toString();
    }
}
