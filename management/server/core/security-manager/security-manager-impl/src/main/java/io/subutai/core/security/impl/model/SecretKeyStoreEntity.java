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

    private static final String KEY_FINGERPRINT = "kfingerprint";
    private static final String STATUS = "status";
    private static final String TYPE = "type";
    private static final String DATA = "data";
    private static final String PWD = "pwd";


    @Id
    @Column( name = KEY_FINGERPRINT )
    private String keyFingerprint;

    @Column( name = STATUS )
    private short status = 1;

    @Column( name = TYPE )
    private int type = 1;

    @Column( name = PWD )
    private String pwd;

    @Lob
    @Column( name = DATA )
    private byte[] data;


    public String getKeyFingerprint()
    {
        return keyFingerprint;
    }


    public void setKeyFingerprint( final String keyFingerprint )
    {
        this.keyFingerprint = keyFingerprint;
    }


    public short getStatus()
    {
        return status;
    }


    public void setStatus( final short status )
    {
        this.status = status;
    }


    public int getType()
    {
        return type;
    }


    public void setType( final int type )
    {
        this.type = type;
    }


    public String getPwd()
    {
        return pwd;
    }


    public void setPwd( final String pwd )
    {
        this.pwd = pwd;
    }


    public byte[] getData()
    {
        return data;
    }


    public void setData( final byte[] data )
    {
        this.data = data;
    }
}
