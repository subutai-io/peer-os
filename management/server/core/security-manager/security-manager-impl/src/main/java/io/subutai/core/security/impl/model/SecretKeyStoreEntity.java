package io.subutai.core.security.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import io.subutai.core.security.api.model.SecretKeyStore;


/**
 * Stores Secret Keyrings.
 */
@Entity
@Table( name = SecretKeyStoreEntity.TABLE_NAME )
@Access( AccessType.FIELD )
public class SecretKeyStoreEntity implements SecretKeyStore
{

    /********* Table name *********/
    static final String TABLE_NAME = "secret_key_store";

    /********* column names *******/

    private static final String KEY_FINGERPRINT_NAME = "kfingerprint";
    private static final String STATUS_NAME = "status";
    private static final String TYPE_NAME = "type";
    private static final String DATA_NAME = "data";
    private static final String PWD_NAME = "pwd";


    @Id
    @Column( name = KEY_FINGERPRINT_NAME )
    private String keyFingerprint;

    @Column( name = STATUS_NAME )
    private short status = 1;

    @Column( name = TYPE_NAME )
    private int type = 1;

    @Column( name = PWD_NAME )
    private String pwd;

    @Lob
    @Column( name = DATA_NAME )
    private byte[] data;


    @Override
    public String getKeyFingerprint()
    {
        return keyFingerprint;
    }


    @Override
    public void setKeyFingerprint( final String keyFingerprint )
    {
        this.keyFingerprint = keyFingerprint;
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
    public String getPwd()
    {
        return pwd;
    }


    @Override
    public void setPwd( final String pwd )
    {
        this.pwd = pwd;
    }


    @Override
    public byte[] getData()
    {
        return data;
    }


    @Override
    public void setData( final byte[] data )
    {
        this.data = data;
    }
}
