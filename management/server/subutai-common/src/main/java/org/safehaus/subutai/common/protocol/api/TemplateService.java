package org.safehaus.subutai.common.protocol.api;


import java.util.List;

import org.safehaus.subutai.common.protocol.Template;


/**
 * Created by talas on 10/28/14.
 */
public interface TemplateService
{
    public Template saveTemplate( Template templateClone );

    public Template getTemplate( long id );

    public List<Template> getAllTemplates();

    public void removeTemplate( long id );

    public void removeTemplate( Template template );

    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch );


    public Template getTemplateByName( String templateName, String lxcArch );
}
