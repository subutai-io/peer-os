package io.subutai.core.kurjun.impl;


import java.io.Serializable;
import java.net.URL;


public class RepoUrl implements Serializable
{
    private URL url;
    private boolean useToken;


    public RepoUrl( URL url, boolean useToken )
    {
        this.url = url;
        this.useToken = useToken;
    }


    public URL getUrl()
    {
        return url;
    }


    public void setUrl( URL url )
    {
        this.url = url;
    }


    public boolean isUseToken()
    {
        return useToken;
    }


    public void setUseToken( boolean useToken )
    {
        this.useToken = useToken;
    }


    @Override
    public String toString()
    {
        return "RepoUrl{" + "url=" + url + ", useToken=" + useToken + '}';
    }

}
