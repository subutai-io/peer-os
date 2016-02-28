package io.subutai.core.kurjun.impl;


import java.io.Serializable;
import java.net.URL;


public class RepoUrl implements Serializable
{
    private URL url;
    private String token;


    public RepoUrl( URL url, String token )
    {
        this.url = url;
        this.token = token;
    }


    public URL getUrl()
    {
        return url;
    }


    public void setUrl( URL url )
    {
        this.url = url;
    }


    public String getToken()
    {
        return token;
    }


    public void setToken( String token )
    {
        this.token = token;
    }


    @Override
    public String toString()
    {
        return "RepoUrl{" + "url=" + url + ", useToken=" + ( token != null ) + '}';
    }

}
