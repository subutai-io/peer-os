package org.safehaus.subutai.core.shiro.ui.tabs;


import java.io.Serializable;


/**
 * Created by talas on 1/26/15.
 */
public class Permission implements Serializable
{
    String name;


    public Permission( String name )
    {
        this.name = name;
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
