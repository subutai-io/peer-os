package org.safehaus.subutai.common.protocol.api;


import java.util.List;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.protocol.Template;


/**
 * Created by talas on 10/28/14.
 */
public interface TemplateService
{
    public Template saveTemplate( Template templateClone ) throws DaoException;

    public List<Template> getAllTemplates() throws DaoException;

    public void removeTemplate( Template template ) throws DaoException;

    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch ) throws DaoException;


    public Template getTemplateByName( String templateName, String lxcArch ) throws DaoException;
}
