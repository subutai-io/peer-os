package io.subutai.core.security.rest.model;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import io.subutai.core.security.api.model.SecurityKeyIdentity;


/**
 * Created by talas on 11/26/15.
 */
public class KeyIdentityDTO implements SecurityKeyIdentity
{
    private String hostId;
    private String publicKeyFingerprint;
    private String secretKeyFingerprint;
    private short status;
    private int type;
    private boolean child;
    private int trustLevel;
    private List<KeyIdentityDTO> trustedKeys = Lists.newArrayList();
    private String parentId;


    public KeyIdentityDTO( SecurityKeyIdentity securityKeyIdentity )
    {
        this.hostId = securityKeyIdentity.getHostId();
        this.publicKeyFingerprint = securityKeyIdentity.getPublicKeyFingerprint();
        this.secretKeyFingerprint = securityKeyIdentity.getSecretKeyFingerprint();
        this.status = securityKeyIdentity.getStatus();
        this.type = securityKeyIdentity.getType();
        for ( final SecurityKeyIdentity keyIdentity : securityKeyIdentity.getTrustedKeys() )
        {
            this.trustedKeys.add( new KeyIdentityDTO( keyIdentity ) );
        }
    }


    public String getParentId()
    {
        return parentId;
    }


    public void setParentId( final String parentId )
    {
        this.parentId = parentId;
    }


    public boolean isChild()
    {
        return child;
    }


    public void setChild( final boolean child )
    {
        this.child = child;
    }


    public int getTrustLevel()
    {
        return trustLevel;
    }


    public void setTrustLevel( final int trustLevel )
    {
        this.trustLevel = trustLevel;
    }


    @Override
    public String getHostId()
    {
        return hostId;
    }


    @Override
    public void setHostId( final String hostId )
    {
        this.hostId = hostId;
    }


    @Override
    public String getPublicKeyFingerprint()
    {
        return publicKeyFingerprint;
    }


    @Override
    public void setPublicKeyFingerprint( final String publicKeyFingerprint )
    {
        this.publicKeyFingerprint = publicKeyFingerprint;
    }


    @Override
    public String getSecretKeyFingerprint()
    {
        return secretKeyFingerprint;
    }


    @Override
    public void setSecretKeyFingerprint( final String secretKeyFingerprint )
    {
        this.secretKeyFingerprint = secretKeyFingerprint;
    }


    @Override
    public short getStatus()
    {
        return status;
    }


    @Override
    public void setStatus( final short status )
    {
        this.status = status;
    }


    @Override
    public int getType()
    {
        return type;
    }


    @Override
    public void setType( final int type )
    {
        this.type = type;
    }


    public void setTrustedDTOKeys( final List<KeyIdentityDTO> trustedKeys )
    {
        this.trustedKeys = trustedKeys;
    }


    public List<KeyIdentityDTO> getTrusts()
    {
        return trustedKeys;
    }


    @JsonIgnore
    @Override
    public List<SecurityKeyIdentity> getTrustedKeys()
    {
        List<SecurityKeyIdentity> tmp = Lists.newArrayList();
        tmp.addAll( trustedKeys );
        return tmp;
    }


    @JsonIgnore
    @Override
    public void setTrustedKeys( final List<SecurityKeyIdentity> trustedKeys )
    {
        this.trustedKeys.clear();
        for ( final SecurityKeyIdentity trustedKey : trustedKeys )
        {
            this.trustedKeys.add( new KeyIdentityDTO( trustedKey ) );
        }
    }
}
