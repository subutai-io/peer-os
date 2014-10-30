/**
 * Created by talas on 10/30/14.
 */
package org.safehaus.subutai.common.template.api;


import java.util.List;

import org.safehaus.subutai.common.template.Template;


/**
 * Created by talas on 10/28/14.
 */
public interface TemplateService
{
    public Template createTemplate( Template templateClone );

    public Template getTemplate( long id );

    public List<Template> getTemplates();

    public void deleteTemplate( long id );
}
