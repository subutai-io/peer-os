package io.subutai.core.template.api;


import java.util.List;
import java.util.Set;

import io.subutai.common.protocol.Templat;


/**
 * Exposes API to work with templates. All methods use caching except those that explicitly mention otherwise
 */
public interface TemplateManager
{
    /**
     * Returns all templates visible to current user
     */
    Set<Templat> getTemplates();

    /**
     * Returns template by its id
     */
    Templat getTemplate( String id );

    /**
     * Returns template by name. First looks in verified templates, if not found looks in the rest templates.
     *
     * @param name name of template
     */
    Templat getTemplateByName( String name );

    /**
     * Returns template by name from the verified repository. Verified repo contains official templates, not user
     * templates.
     */
    Templat getVerifiedTemplateByName( final String name );

    /**
     * Returns templates belonging to owner
     */
    List<Templat> getTemplatesByOwner( final String owner );

    /**
     * Resets template cache
     */
    void resetTemplateCache();

    String getFingerprint();

    String getObtainedCdnToken();

    String obtainCdnToken( String signedFingerprint );

    boolean isRegisteredWithCdn();

    String getOwner( String token );

    void registerTemplate( Templat templat, String cdnToken );

    List<Templat> getOwnTemplates();
}
