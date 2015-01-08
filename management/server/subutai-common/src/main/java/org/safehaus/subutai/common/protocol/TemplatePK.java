package org.safehaus.subutai.common.protocol;


import java.io.Serializable;


/**
 * Created by talas on 11/5/14.
 */ //@Embeddable
public class TemplatePK implements Serializable
{
    //        @Column(name = "templateName")
    private String templateName;
    //        @Column(name = "lxcArch")
    private String lxcArch;

    private String templateVersion;

    private String md5sum;


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


    public String getTemplateVersion()
    {
        return templateVersion;
    }


    public void setTemplateVersion( final String templateVersion )
    {
        this.templateVersion = templateVersion;
    }


    public String getMd5sum()
    {
        return md5sum;
    }


    public void setMd5sum( final String md5sum )
    {
        this.md5sum = md5sum;
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

        return lxcArch.equals( that.lxcArch ) && md5sum.equals( that.md5sum ) && templateName
                .equals( that.templateName ) && templateVersion.equals( that.templateVersion );
    }


    @Override
    public int hashCode()
    {
        int result = templateName.hashCode();
        result = 31 * result + lxcArch.hashCode();
        result = 31 * result + templateVersion.hashCode();
        result = 31 * result + md5sum.hashCode();
        return result;
    }
}
