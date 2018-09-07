package io.subutai.bazaar.share.dto.environment;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class SSHKeyDto
{
    private String name;
    private Date createDate;
    private String sshKey;
    private Set<String> configuredPeers = new HashSet<>();


    public SSHKeyDto()
    {
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public Date getCreateDate()
    {
        return createDate;
    }


    public void setCreateDate( final Date createDate )
    {
        this.createDate = createDate;
    }


    public String getSshKey()
    {
        return sshKey;
    }


    public void setSshKey( final String sshKey )
    {
        this.sshKey = sshKey;
    }


    public Set<String> getConfiguredPeers()
    {
        return configuredPeers;
    }


    public void setConfiguredPeers( final Set<String> configuredPeers )
    {
        this.configuredPeers = configuredPeers;
    }


    public void addConfiguredPeer( final String peerId )
    {
        this.configuredPeers.add( peerId );
    }
}
