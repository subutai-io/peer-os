package io.subutai.common.protocol;


import com.google.gson.annotations.Expose;


public class Resource
{
    @Expose
    private String md5Sum;
    @Expose
    private String name;


    public Resource( String md5Sum, String name )
    {
        this.md5Sum = md5Sum;
        this.name = name;
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

}
