package org.safehaus.subutai.core.identity.impl.entity;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.safehaus.subutai.core.identity.api.RestEndpointScope;

import com.google.common.base.Preconditions;


@Entity
@Access( AccessType.FIELD )
@Table( name = "rest_endpoints" )
public class RestEndpointScopeEntity implements Serializable, RestEndpointScope
{
    @Id
    @Column(name = "rest_endpoint")
    private String restEndpoint = "rest_endpoint";

    @Column(name = "rest_port")
    private Long port;


    public RestEndpointScopeEntity( final String restEndpoint )
    {
        Preconditions.checkNotNull( restEndpoint, "Invalid argument restEndpoint" );
        this.restEndpoint = restEndpoint;
    }

    @Override
    public String getRestEndpoint()
    {
        return restEndpoint;
    }


    @Override
    public Long getPort()
    {
        return port;
    }
}
