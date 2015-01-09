package org.safehaus.subutai.core.peer.api;


import java.util.List;

import org.safehaus.subutai.common.protocol.Template;


/**
 * Order for container cloning process.
 */
public class CloneParam implements HostTaskParam
{
    private String hostname;
    private List<Template> templates;


    public CloneParam( final String hostname, List<Template> templates )
    {
        this.hostname = hostname;
        this.templates = templates;
    }


    public String getHostname()
    {
        return hostname;
    }


    public List<Template> getTemplates()
    {
        return templates;
    }


    /**
     * Returns the template name from them resource host will clone a container. Assumed that is last in the list.
     */
    public String getTemplateName()
    {
        return templates.get( templates.size() - 1 ).getTemplateName();
    }
}
