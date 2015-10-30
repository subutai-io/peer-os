package io.subutai.core.identity.api.model;


import java.util.Date;

import javax.security.auth.Subject;


/**
 *
 */
public interface Session
{
    Long getId();

    void setId( Long id );

    User getUser();

    void setUser( User user );

    boolean isActive();

    void setActive( boolean active );

    Date getStartDate();

    void setStartDate( Date startDate );

    Date getEndDate();

    void setEndDate( Date endDate );

    Subject getSubject();

    void setSubject( Subject subject );
}
