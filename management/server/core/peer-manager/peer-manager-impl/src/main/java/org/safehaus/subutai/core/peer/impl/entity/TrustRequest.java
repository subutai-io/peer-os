package org.safehaus.subutai.core.peer.impl.entity;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;


/**
 * Created by nisakov on 2/24/15.
 */
@Entity
@Table( name = "trust_handshake" )

public class TrustRequest
{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column( name = "id" )
    private long id;

    @Column( name = "peer_id" )
    private String peer_id;

    @Column( name = "response_id" )
    private long response_id;

    @Lob
    @Column( name = "root_cert_px2" )
    private String RequestRootCertPx2;

    @Column( name = "status" )
    private short status;

    @Column( name = "type" )
    private short type;

    @Column(name = "date")
    private java.sql.Timestamp date;


}
