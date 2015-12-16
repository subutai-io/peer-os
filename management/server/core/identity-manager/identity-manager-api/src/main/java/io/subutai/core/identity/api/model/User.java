package io.subutai.core.identity.api.model;


import java.util.List;


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

    String getSecurityKeyId();

    void setSecurityKeyId( String securityKeyId );

    String getStatusName();

    String getTypeName();

    boolean isApproved();

    void setApproved(boolean active);

}
