package org.safehaus.subutai.common.datatypes;


import java.io.Serializable;

import org.safehaus.subutai.common.settings.Common;

import com.google.gson.annotations.Expose;


/**
 * Created by talas on 12/19/14.
 */

public class TemplateVersion implements Serializable
{
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
