package io.subutai.core.template.api;


import java.util.List;
import java.util.Set;

import io.subutai.common.protocol.Template;


public interface TemplateManager
{
    Set<Template> getTemplates();

    Template getTemplate( String id );

    /**
     * Returns template by name. First looks in verified templates, if not found looks in the rest templates.
     *
     * @param name name of template
     */
    Template getTemplateByName( String name );

    Template getVerifiedTemplateByName( final String name );

    List<Template> getTemplatesByOwner( final String owner );

    /**
     * Returns active users templates (templates that contain active user fingerprint in owners list)
     * Returns always fresh list without caching
     */
    List<Template> getUserPrivateTemplates();

    void resetTemplateCache();
}
