package io.subutai.core.security.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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

    public static final String HOST_ID = "peer_id";
    public static final String KEY_FINGERPRINT  = "kfingerprint";
    public static final String STATUS  = "status";
    public static final String TYPE    = "type";


    @Id
    @Column( name = HOST_ID )
    private String hostId;

    @Column( name = KEY_FINGERPRINT )
    private String keyFingerprint;

    @Column( name = STATUS )
    private short status;

    @Column( name = TYPE )
    private short type;

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
    public short getType()
    {
        return type;
    }


    @Override
    public void setType( final short type )
    {
        this.type = type;
    }
}
