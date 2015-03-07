package org.safehaus.subutai.core.identity.api;


/**
 * Created by talas on 3/7/15.
 */


/**
 * Interface class for obscuration of entity class which is used for revoking system users access to certain modules.
 * {@link UserPortalModule#getModuleKey()} - used to identify module by key {@link UserPortalModule#getModuleName()} -
 * used to identify module by name
 */
public interface UserPortalModule
{
    public String getModuleName();

    public String getModuleKey();
}
