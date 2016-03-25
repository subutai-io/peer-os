package io.subutai.common.protocol;


import com.google.gson.annotations.Expose;


public class Resource
{
    @Expose
    private String md5Sum;
    @Expose
    private String name;
    @Expose
    private long size;


    public Resource( String md5Sum, String name, long size )
    {
        this.md5Sum = md5Sum;
        this.name = name;
        this.size = size;
    }


    public String getMd5Sum()
    {
        return md5Sum;
    }


    public void setMd5Sum( String md5Sum )
    {
        this.md5Sum = md5Sum;
    }


    public String getName()
    {
        return name;
    }


    public void setName( String name )
    {
        this.name = name;
    }


    public long getSize()
    {
        return size;
    }


    public void setSize( long size )
    {
        this.size = size;
    }

}
