package org.safehaus.subutai.core.security.api;


import java.util.List;


/**
 * Created by timur on 1/21/15.
 */
public interface User
{
//    public List<Role> getRoles();

    public Long getId();

    public String getPassword();

    public List<String> getPermissions();
}
