package org.safehaus.subutai.common.datatypes;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.safehaus.subutai.common.settings.Common;

import com.google.gson.annotations.Expose;


/**
 * Created by talas on 12/19/14.
 */

@Entity
@Table( name = "template_version" )
@Access( AccessType.FIELD )
public class TemplateVersion implements Serializable
{
    @Id
    @Column( name = "template_version" )
    @Expose
    private String templateVersion = Common.DEFAULT_TEMPLATE_VERSION;


    public TemplateVersion( final String templateVersion )
    {
        this.templateVersion = templateVersion;
    }


    public String getTemplateVersion()
    {
        return templateVersion;
    }


    @Override
    public String toString()
    {
        return templateVersion;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof TemplateVersion ) )
        {
            return false;
        }

        final TemplateVersion that = ( TemplateVersion ) o;

        return templateVersion.equals( that.templateVersion );
    }


    @Override
    public int hashCode()
    {
        return templateVersion.hashCode();
    }
}
