package io.subutai.core.gateway.rest;


import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class UserCredentials
{
    private String username;
    private String password;


    public String getUsername()
    {
        return username;
    }


    public void setUsername( final String username )
    {
        this.username = username;
    }


    public String getPassword()
    {
        return password;
    }


    public void setPassword( final String password )
    {
        this.password = password;
    }
}
