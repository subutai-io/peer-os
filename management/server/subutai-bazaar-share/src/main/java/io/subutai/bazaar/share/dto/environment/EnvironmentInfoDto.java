package io.subutai.bazaar.share.dto.environment;


import java.util.HashSet;
import java.util.Set;


public class EnvironmentInfoDto
{
    private String id;

    private Long bazaarId;

    private String ownerId;

    private String ownerName;

    private Long ssOwnerId;

    private String name;

    private String subnetCidr;

    private String p2pSubnet;

    private EnvironmentDto.State state;

    private String publicKey;

    private Integer p2pTTL;

    private String p2pHash;

    private String p2pKey;

    private String description;

    private String domainName;

    private String domainLoadBalanceStrategy;

    private String sslCertPath;

    private Long vni;

    private String VEHS;

    private Set<SSHKeyDto> sshKeys = new HashSet<>();


    public EnvironmentInfoDto()
    {
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public Long getbazaarId()
    {
        return bazaarId;
    }


    public void setbazaarId( Long bazaarId )
    {
        this.bazaarId = bazaarId;
    }


    public String getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( String ownerId )
    {
        this.ownerId = ownerId;
    }


    public String getOwnerName()
    {
        return ownerName;
    }


    public void setOwnerName( final String ownerName )
    {
        this.ownerName = ownerName;
    }


    public Long getSsOwnerId()
    {
        return ssOwnerId;
    }


    public void setSsOwnerId( Long ssOwnerId )
    {
        this.ssOwnerId = ssOwnerId;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getSubnetCidr()
    {
        return subnetCidr;
    }


    public void setSubnetCidr( final String subnetCidr )
    {
        this.subnetCidr = subnetCidr;
    }


    public EnvironmentDto.State getState()
    {
        return state;
    }


    public void setState( final EnvironmentDto.State state )
    {
        this.state = state;
    }


    public String getPublicKey()
    {
        return publicKey;
    }


    public void setPublicKey( final String publicKey )
    {
        this.publicKey = publicKey;
    }


    public Integer getP2pTTL()
    {
        return p2pTTL;
    }


    public void setP2pTTL( final Integer p2pTTL )
    {
        this.p2pTTL = p2pTTL;
    }


    public String getP2pHash()
    {
        return p2pHash;
    }


    public void setP2pHash( final String p2pHash )
    {
        this.p2pHash = p2pHash;
    }


    public String getP2pKey()
    {
        return p2pKey;
    }


    public void setP2pKey( final String p2pKey )
    {
        this.p2pKey = p2pKey;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription( final String description )
    {
        this.description = description;
    }


    public Long getVni()
    {
        return vni;
    }


    public void setVni( final Long vni )
    {
        this.vni = vni;
    }


    public String getP2pSubnet()
    {
        return p2pSubnet;
    }


    public void setP2pSubnet( final String p2pSubnet )
    {
        this.p2pSubnet = p2pSubnet;
    }


    public Set<SSHKeyDto> getSshKeys()
    {
        return sshKeys;
    }


    public void setSshKeys( final Set<SSHKeyDto> sshKeys )
    {
        this.sshKeys = sshKeys;
    }


    public void addSshKey( final SSHKeyDto sshKey )
    {
        this.sshKeys.add( sshKey );
    }


    public String getVEHS()
    {
        return VEHS;
    }


    public void setVEHS( final String VEHS )
    {
        this.VEHS = VEHS;
    }


    public String getDomainName()
    {
        return domainName;
    }


    public void setDomainName( final String domainName )
    {
        this.domainName = domainName;
    }


    public String getDomainLoadBalanceStrategy()
    {
        return domainLoadBalanceStrategy;
    }


    public void setDomainLoadBalanceStrategy( final String domainLoadBalanceStrategy )
    {
        this.domainLoadBalanceStrategy = domainLoadBalanceStrategy;
    }


    public String getSslCertPath()
    {
        return sslCertPath;
    }


    public void setSslCertPath( final String sslCertPath )
    {
        this.sslCertPath = sslCertPath;
    }
}
