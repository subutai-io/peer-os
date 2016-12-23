package io.subutai.core.template.api;


import java.util.Set;

import io.subutai.common.protocol.Template;


public interface TemplateManager
{
    Set<Template> getTemplates();

    Template getTemplate( String id );

    Template getTemplateByName( String name );

    Template getVerifiedTemplateByName( final String name );
}
