package org.safehaus.subutai.common.protocol;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;

import org.safehaus.subutai.common.datatypes.TemplateVersion;

import com.google.gson.annotations.Expose;


@Embeddable
@Table( name = "template_pk" )
@Access( AccessType.FIELD )
public class TemplatePK implements Serializable
{
    @Column( name = "template_name" )
    @Expose
    private String templateName;
    //        @Column(name = "lxcArch")
    @Expose
    @Column( name = "lxc_arch" )
    private String lxcArch;


    @Expose
    @Column( name = "template_version" )
    private String templateVersion;


    @Expose
    @Column( name = "md5_sum" )
    private String md5sum;


    public TemplatePK()
    {
    }


    public TemplatePK( final String templateName, final String lxcArch, final TemplateVersion templateVersion,
                       final String md5sum )
    {
        this.templateName = templateName;
        this.lxcArch = lxcArch;
        this.templateVersion = templateVersion.toString();
        this.md5sum = md5sum;
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


    public TemplateVersion getTemplateVersion()
    {
        return new TemplateVersion( templateVersion );
    }


    public void setTemplateVersion( final TemplateVersion templateVersion )
    {
        this.templateVersion = templateVersion.toString();
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
