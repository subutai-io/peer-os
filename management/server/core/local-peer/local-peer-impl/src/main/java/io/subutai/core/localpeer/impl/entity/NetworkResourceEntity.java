package io.subutai.core.localpeer.impl.entity;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.peer.NetworkResource;
import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;


@Entity
@Table( name = "net_resource" )
@Access( AccessType.FIELD )
public class NetworkResourceEntity implements NetworkResource
{
    @Id
    @Column( name = "environment_id" )
    @JsonProperty( "environmentId" )
    protected String environmentId;

    @Column( name = "vni" )
    @JsonProperty( "vni" )
    private long vni;

    @Column( name = "p2p_subnet" )
    @JsonProperty( "p2pSubnet" )
    private String p2pSubnet;

    @Column( name = "container_subnet" )
    @JsonProperty( "containerSubnet" )
    private String containerSubnet;


    protected NetworkResourceEntity()
    {

    }


    public NetworkResourceEntity( final String environmentId, final Long vni, final String p2pSubnet,
                                  final String containerSubnet )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pSubnet ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerSubnet ) );
        Preconditions.checkArgument( NumUtil.isLongBetween( vni, Common.MIN_VNI_ID, Common.MAX_VNI_ID ) );

        this.environmentId = environmentId;
        this.vni = vni;
        this.p2pSubnet = p2pSubnet;
        this.containerSubnet = containerSubnet;
    }


    @Override
    public String getEnvironmentId()
    {
        return environmentId;
    }


    @Override
    public long getVni()
    {
        return vni;
    }


    @Override
    public String getP2pSubnet()
    {
        return p2pSubnet;
    }


    @Override
    public String getContainerSubnet()
    {
        return containerSubnet;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "environmentId", environmentId ).add( "vni", vni )
                          .add( "containerSubnet", containerSubnet ).add( "p2pSubnet", p2pSubnet ).toString();
    }
}
