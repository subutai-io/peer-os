package io.subutai.core.identity.api;


/**
 * Interface class for obscuration of entity class which is used for revoking system users access to certain modules.
 * {@link PortalModuleScope#getModuleKey()} - used to identify module by key {@link PortalModuleScope#getModuleName()} -
 * used to identify module by name
 */
public interface PortalModuleScope
{
    public String getModuleName();

    public String getModuleKey();
}
