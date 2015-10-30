package io.subutai.core.identity.impl.model;


import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

    @Column(name="active")
    private boolean active;

    @Column(name="start_date")
    private Date startDate;

    @Column(name="end_date")
    private Date endDate;

    //************************************
    @ManyToOne
    @JoinColumn( name = "user_id" )
    private User user;
    //************************************


    //************************************
    @Transient
    private Subject subject;
    //************************************

    @Override
    public Long getId()
    {
        return id;
    }


    @Override
    public void setId( final Long id )
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
    public boolean isActive()
    {
        return active;
    }


    @Override
    public void setActive( final boolean active )
    {
        this.active = active;
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
}
