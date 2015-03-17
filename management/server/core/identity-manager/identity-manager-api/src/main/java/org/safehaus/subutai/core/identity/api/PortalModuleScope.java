package org.safehaus.subutai.core.identity.api;


/**
 * Created by talas on 3/7/15.
 */


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
