package io.subutai.core.kurjun.impl.model;


import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;


public class RepoUrl implements Serializable
{
    private String urlString;
    private String token;


    public RepoUrl( URL url, String token )
    {
        this.urlString = url.toExternalForm().replaceAll( "/", "@" ).replaceAll( ":", "#" );
        this.token = token;
    }


    public String getUrlString()
    {
        return urlString;
    }


    public void setUrlString( final String urlString )
    {
        this.urlString = urlString;
    }


    public URL getUrl()
    {
        try
        {
            return new URL( urlString.replaceAll( "@", "/" ).replaceAll( "#", ":" ) );
        }
        catch ( MalformedURLException e )
        {
            return null;
        }
    }


    public void setUrl( URL url )
    {
        this.urlString = url.toExternalForm().replaceAll( "/", "@" ).replaceAll( ":", "#" );
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
        return "RepoUrl{" + "urlString=" + urlString + ", useToken=" + ( token != null ) + '}';
    }
}
