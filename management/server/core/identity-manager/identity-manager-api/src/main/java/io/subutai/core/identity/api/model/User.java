package io.subutai.core.identity.api.model;


import java.util.List;

import javax.security.auth.Subject;


public interface User
{

    Long getId();

    void setId( Long id );

    String getUserName();

    void setUserName( String userName );

    String getFullName();

    void setFullName( String fullName );

    String getPassword();

    void setPassword( String password );

    String getSalt();

    void setSalt( String salt );

    String getEmail();

    void setEmail( String email );

    List<Role> getRoles();

    void setRoles( List<Role> roles );

    int getType();

    void setType( int type );

    int getStatus();

    void setStatus( int status );

    int getSecurityKeyId();

    void setSecurityKeyId( int securityKeyId );

    String getStatusName();

    String getTypeName();

    Subject getSubject();

    void setSubject( Subject subject );
}
