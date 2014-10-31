package org.safehaus.subutai.common.protocol.api;


import java.util.List;

import org.safehaus.subutai.common.protocol.Template;


/**
 * Created by talas on 10/28/14.
 */
public interface TemplateService
{
    public Template createTemplate( Template templateClone );

    public Template getTemplate( long id );

    public List<Template> getTemplates();

    public void deleteTemplate( long id );

    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch );


    public Template getTemplateByName( String templateName, String lxcArch );
}
