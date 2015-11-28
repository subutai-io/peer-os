package io.subutai.core.security.impl.model;


import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.collect.Lists;

import io.subutai.core.security.api.model.SecurityKeyIdentity;


/**
 * Implementation of Security Identity Data
 */

@Entity
@Table( name = SecurityKeyIdentityEntity.TABLE_NAME )
@Access( AccessType.FIELD )
public class SecurityKeyIdentityEntity implements SecurityKeyIdentity
{
    /********* Table name *********/
    public static final String TABLE_NAME = "security_key_identity";

    /********* column names *******/

    public static final String IDENTITY_ID = "identity_id";
    public static final String PUBLIC_KEY_FINGERPRINT  = "pkfingerprint";
    public static final String SECRET_KEY_FINGERPRINT  = "skfingerprint";
    public static final String STATUS  = "status";
    public static final String TYPE    = "type";


    @Id
    @Column( name = IDENTITY_ID )
    private String identityId;

    @Column( name = PUBLIC_KEY_FINGERPRINT )
    private String publicKeyFingerprint;

    @Column( name = SECRET_KEY_FINGERPRINT )
    private String secretKeyFingerprint;

    @Column( name = STATUS )
    private short status;

    @Column( name = TYPE )
    private int type;

    @Transient
    private List<SecurityKeyIdentity> trustedKeys = Lists.newArrayList();


    @Override
    public List<SecurityKeyIdentity> getTrustedKeys()
    {
        return trustedKeys;
    }


    @Override
    public void setTrustedKeys( final List<SecurityKeyIdentity> trustedKeys )
    {
        this.trustedKeys = trustedKeys;
    }


    @Override
    public String getIdentityId()
    {
        return identityId;
    }


    @Override
    public void setIdentityId( final String identityId )
    {
        this.identityId = identityId;
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
}
