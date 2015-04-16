package org.safehaus.subutai.common.protocol.api;


import java.util.List;

import org.safehaus.subutai.common.datatypes.TemplateVersion;
import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.protocol.Template;


public interface TemplateService
{
    public Template saveTemplate( Template templateClone ) throws DaoException;

    public List<Template> getAllTemplates() throws DaoException;

    public void removeTemplate( Template template ) throws DaoException;

    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch ) throws DaoException;


    public List<Template> getChildTemplates( String parentTemplateName, TemplateVersion templateVersion,
                                             String lxcArch ) throws DaoException;


    public Template getTemplate( String templateName, String lxcArch ) throws DaoException;


    public Template getTemplate( String templateName, String lxcArch, String md5sum, TemplateVersion version )
            throws DaoException;

    public Template getTemplate( String templateName, TemplateVersion templateVersion, String lxcArch )
            throws DaoException;
}
