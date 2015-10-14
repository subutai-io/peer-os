package io.subutai.core.identity.impl.model;


import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
    @GeneratedValue
    @Column( name = "id" )
    private Long id;

    @Column(name="active")
    private boolean active;

    @Column(name="start_date")
    private Date startDate;

    @Column(name="end_date")
    private Date endDate;


    @ManyToOne
    @JoinColumn( name = "user_id", referencedColumnName = "id" )
    private User user;


    public Long getId()
    {
        return id;
    }


    public void setId( final Long id )
    {
        this.id = id;
    }


    public User getUser()
    {
        return user;
    }


    public void setUser( final User user )
    {
        this.user = user;
    }


    public boolean isActive()
    {
        return active;
    }


    public void setActive( final boolean active )
    {
        this.active = active;
    }


    public Date getStartDate()
    {
        return startDate;
    }


    public void setStartDate( final Date startDate )
    {
        this.startDate = startDate;
    }


    public Date getEndDate()
    {
        return endDate;
    }


    public void setEndDate( final Date endDate )
    {
        this.endDate = endDate;
    }
}
