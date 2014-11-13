package org.safehaus.subutai.common.protocol;


import java.io.Serializable;


/**
 * Created by talas on 11/5/14.
 */ //@Embeddable
public class TemplatePK implements Serializable
{
    //        @Column(name = "templateName")
    String templateName;
    //        @Column(name = "lxcArch")
    String lxcArch;


    public TemplatePK()
    {
    }


    public TemplatePK( String templateName, String lxcArch )
    {
        this.templateName = templateName;
        this.lxcArch = lxcArch;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getLxcArch()
    {
        return lxcArch;
    }


    public void setLxcArch( final String lxcArch )
    {
        this.lxcArch = lxcArch;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof TemplatePK ) )
        {
            return false;
        }

        final TemplatePK that = ( TemplatePK ) o;

        return lxcArch.equals( that.lxcArch ) && templateName.equals( that.templateName );
    }


    @Override
    public int hashCode()
    {
        int result = templateName.hashCode();
        result = 31 * result + lxcArch.hashCode();
        return result;
    }
}
