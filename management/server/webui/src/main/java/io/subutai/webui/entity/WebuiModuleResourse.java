package io.subutai.webui.entity;


import java.util.List;

public class WebuiModuleResourse
{
    private String img;
    private String name;
    private String angularPath;
    private List<AngularjsDependency> dependencies;

    private String bodyClass;
    private String layout;


    public String getImg()
    {
        return img;
    }


    public void setImg( final String img )
    {
        this.img = img;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }
}
