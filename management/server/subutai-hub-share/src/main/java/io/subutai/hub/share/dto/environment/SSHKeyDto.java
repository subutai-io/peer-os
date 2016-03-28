package io.subutai.hub.share.dto.environment;


import java.util.Date;


public class SSHKeyDto
{
    private String name;
    private Date createDate;
    private String sshKey;


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
}
