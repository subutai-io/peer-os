package io.subutai.core.security.api.jetty;


public interface HttpContextManager
{
    /**
     * Reloads Jetty keystores
     */
    void reloadKeyStore();
}