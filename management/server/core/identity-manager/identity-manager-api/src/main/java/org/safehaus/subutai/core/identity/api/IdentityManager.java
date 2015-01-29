package org.safehaus.subutai.core.identity.api;


import java.io.Serializable;
import java.util.List;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;


public interface IdentityManager
{
    public SecurityManager getSecurityManager();

    public Subject login( AuthenticationToken token );

    public Subject getSubject( Serializable sessionId );

    public void logout( Serializable sessionId );

    public List<User> getAllUsers();

    public boolean addUser( String username, String fullname, String password, String email );
}

