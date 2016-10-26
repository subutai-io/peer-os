package io.subutai.core.keyserver.impl.model;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import io.subutai.core.keyserver.api.model.PublicKeyStore;


/**
 * Entity for Security object storage
 */

@Entity
@Table( name = PublicKeyStoreEntity.TABLE_NAME )
@Access( AccessType.FIELD )

public class PublicKeyStoreEntity implements PublicKeyStore, Serializable
{
    /********* Table name *********/
    public static final String TABLE_NAME = "public_key_store";

    /********* column names *******/
    public static final String KEY_ID_SHORT_NAME = "short_key_id";
    public static final String KEY_ID_NAME = "key_id";
    public static final String FINGERPRINT_NAME = "fingerprint";
    public static final String KEY_DATA_NAME = "data";
    public static final String KEY_TYPE_NAME = "type";
    public static final String KEY_STATUS_NAME = "status";


    @Id
    @Column( name = KEY_ID_NAME )
    private String keyId;

    @Column( name = KEY_ID_SHORT_NAME )
    private String shortKeyId;

    @Column( name = FINGERPRINT_NAME )
    private String fingerprint;

    @Lob
    @Column( name = KEY_DATA_NAME )
    private byte[] keyData;

    @Column( name = KEY_TYPE_NAME )
    private short keyType = 1;

    @Column( name = KEY_STATUS_NAME )
    private short keyStatus = 1;


    /********************************/

    public String getKeyId()
    {
        return keyId;
    }


    public void setKeyId( final String keyId )
    {
        this.keyId = keyId;
    }


    public String getShortKeyId()
    {
        return shortKeyId;
    }


    public void setShortKeyId( final String shortKeyId )
    {
        this.shortKeyId = shortKeyId;
    }


    public String getFingerprint()
    {
        return fingerprint;
    }


    public void setFingerprint( final String fingerprint )
    {
        this.fingerprint = fingerprint;
    }


    public byte[] getKeyData()
    {
        return keyData;
    }


    public void setKeyData( final byte[] keyData )
    {
        this.keyData = keyData;
    }


    public short getKeyType()
    {
        return keyType;
    }


    public void setKeyType( final short keyType )
    {
        this.keyType = keyType;
    }


    public short getKeyStatus()
    {
        return keyStatus;
    }


    public void setKeyStatus( final short keyStatus )
    {
        this.keyStatus = keyStatus;
    }


}
