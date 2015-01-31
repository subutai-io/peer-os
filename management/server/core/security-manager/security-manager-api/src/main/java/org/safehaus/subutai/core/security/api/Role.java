package org.safehaus.subutai.core.security.api;


import java.util.List;


/**
 * Created by timur on 1/21/15.
 */
public interface Role
{
    public String getName();

    public List<String> getPermissions();
}
