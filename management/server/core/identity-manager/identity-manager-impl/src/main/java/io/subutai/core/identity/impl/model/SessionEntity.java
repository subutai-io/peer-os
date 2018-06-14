package io.subutai.core.identity.impl.model;


import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.security.auth.Subject;

import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;


/**
 *
 */
@Entity
@Table( name = "session" )
@Access( AccessType.FIELD )
public class SessionEntity implements Session
{
    @Id
    @Column( name = "id" )
    private String id;

    @Column( name = "status" )
    private int status = 1;

    @Column( name = "start_date" )
    private Date startDate = new Date( System.currentTimeMillis() );

    @Column( name = "end_date" )
    private Date endDate = new Date( System.currentTimeMillis() );

    //************************************
    @ManyToOne( targetEntity = UserEntity.class )
    @JoinColumn( name = "user_id" )
    private User user;
    //************************************


    //************************************
    @Transient
    private Subject subject;
    //************************************
    private long cdnTokenSetTime;
    private String cdnToken;


    @Override
    public String getId()
    {
        return id;
    }


    @Override
    public void setId( final String id )
    {
        this.id = id;
    }


    @Override
    public User getUser()
    {
        return user;
    }


    @Override
    public void setUser( final User user )
    {
        this.user = user;
    }


    @Override
    public int getStatus()
    {
        return status;
    }


    @Override
    public void setStatus( final int status )
    {
        this.status = status;
    }


    @Override
    public Date getStartDate()
    {
        return startDate;
    }


    @Override
    public void setStartDate( final Date startDate )
    {
        this.startDate = startDate;
    }


    @Override
    public Date getEndDate()
    {
        return endDate;
    }


    @Override
    public void setEndDate( final Date endDate )
    {
        this.endDate = endDate;
    }


    @Override
    public Subject getSubject()
    {
        return subject;
    }


    @Override
    public void setSubject( final Subject subject )
    {
        this.subject = subject;
    }


    @Override
    public synchronized void setCdnToken( final String token )
    {
        cdnToken = token;
        cdnTokenSetTime = System.currentTimeMillis();
    }


    @Override
    public synchronized String getCdnToken()
    {
        //invalidate token after 30 min
        if ( System.currentTimeMillis() - cdnTokenSetTime > TimeUnit.MINUTES.toMillis( 30 ) )
        {
            cdnToken = null;
        }

        return cdnToken;
    }
}
