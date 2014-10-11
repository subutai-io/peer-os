package org.safehaus.subutai.core.template.api;


import java.io.Serializable;
import java.net.URL;


/**
 * Created by timur on 10/7/14.
 */
public class SubutaiPackage implements Serializable
{
    private String name;
    private String description;
    private URL url;
    private String md5;
    private long size;


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription( final String description )
    {
        this.description = description;
    }


    public URL getUrl()
    {
        return url;
    }


    public void setUrl( final URL url )
    {
        this.url = url;
    }


    public String getMd5()
    {
        return md5;
    }


    public void setMd5( final String md5 )
    {
        this.md5 = md5;
    }


    public long getSize()
    {
        return size;
    }


    public void setSize( final long size )
    {
        this.size = size;
    }
}
