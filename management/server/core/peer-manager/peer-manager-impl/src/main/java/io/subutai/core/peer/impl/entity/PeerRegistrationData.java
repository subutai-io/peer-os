package io.subutai.core.peer.impl.entity;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import io.subutai.common.peer.RegistrationData;
import io.subutai.common.util.JsonUtil;


@Entity
@Table( name = "peer_requests" )
@Access( AccessType.FIELD )
public class PeerRegistrationData implements Serializable
{
    @Id
    @Column
    private String id;

    @Column
    @Lob
    private String registrationData;


    public PeerRegistrationData( final String peerId, final RegistrationData registrationData )
    {
        this.id = peerId;
        this.registrationData = JsonUtil.toJson( registrationData );
    }


    public String getId()
    {
        return id;
    }


    public RegistrationData getRegistrationData()
    {
        return JsonUtil.fromJson( registrationData, RegistrationData.class );
    }
}
