package io.subutai.core.template.api;


import java.util.List;
import java.util.Set;

import io.subutai.common.protocol.Template;


/**
 * Exposes API to work with templates. All methods use caching except those that explicitly mention otherwise
 */
public interface TemplateManager
{
    /**
     * Returns all templates visible to current user
     */
    Set<Template> getTemplates();

    /**
     * Returns template by its id
     */
    Template getTemplate( String id );

    /**
     * Returns template by name. First looks in verified templates, if not found looks in the rest templates.
     *
     * @param name name of template
     */
    Template getTemplateByName( String name );

    /**
     * Returns template by name from the verified repository. Verified repo contains official templates, not user
     * templates.
     */
    Template getVerifiedTemplateByName( final String name );

    /**
     * Returns templates belonging to owner
     */
    List<Template> getTemplatesByOwner( final String owner );

    /**
     * Resets template cache
     */
    void resetTemplateCache();

    String getFingerprint();

    String getTokenRequest();

    String getObtainedCdnToken();

    String obtainCdnToken( String signedRequest );

    boolean isRegisteredWithCdn();

    String getOwner( String token );

    List<Template> getOwnTemplates();
}
