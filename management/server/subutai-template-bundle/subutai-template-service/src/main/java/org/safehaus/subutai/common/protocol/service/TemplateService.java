package org.safehaus.subutai.common.protocol.service;


import java.util.List;

import org.safehaus.subutai.common.protocol.Template;


/**
 * Created by talas on 10/28/14.
 */
public interface TemplateService
{
    public Template createTemplate( Template template );

    public Template getTemplate( long id );

    public List<Template> getTemplates();

    public void deleteTemplate( long id );
}
