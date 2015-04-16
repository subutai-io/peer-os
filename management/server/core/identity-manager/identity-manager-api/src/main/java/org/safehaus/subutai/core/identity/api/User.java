package org.safehaus.subutai.core.identity.api;


import java.util.List;
import java.util.Set;


public interface User
{
    public Long getId();

    public void setUsername( String username );

    public String getPassword();

    public void setPassword( String password );


    public boolean isAdmin();

    public List<String> getPermissions();

    public void setSalt( String salt );

    public String getFullname();

    public String getUsername();

    public String getEmail();

    public void removeRole( Role roleEntity );

    public void removeAllRoles( );

    public void setFullname( String fullname );

    public void setEmail( String email );

    public String getKey();

    public void setKey( String key );

    public void addRole( Role role );

    Set<Role> getRoles();

}
