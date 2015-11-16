package io.subutai.core.identity.rest;


import org.codehaus.jackson.annotate.JsonProperty;


public class UserCrdentials
{
    @JsonProperty( "username" )
    private String username;
    @JsonProperty( "password" )
    private String password;


    public UserCrdentials( final String username, final String password )
    {
        this.username = username;
        this.password = password;
    }


    public String getUsername()
    {
        return username;
    }


    public String getPassword()
    {
        return password;
    }
}
