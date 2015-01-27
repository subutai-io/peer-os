package org.safehaus.subutai.core.identity.api;


import java.util.List;


/**
 * Created by timur on 1/21/15.
 */
public interface User
{
    public Long getId();

    public void setUsername( String username );

    public String getPassword();

    public void setPassword( String password );


    public List<String> getPermissions();
}
