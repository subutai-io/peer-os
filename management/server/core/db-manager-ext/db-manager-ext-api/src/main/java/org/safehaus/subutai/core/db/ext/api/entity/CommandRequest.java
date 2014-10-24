package org.safehaus.subutai.core.db.ext.api.entity;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table( name = "command_request" )

public class CommandRequest implements Serializable
{

    private static final long serialVersionUID = 1L;

    @Id
    @Column( name = "command_id" )
    private String commandId;

    @Column( name = "source_peer_id" )
    private String sourcePeerId;

    @Column( name = "create_date" )
    @Temporal( TemporalType.TIMESTAMP )
    private java.util.Date createDate;

    @Column( name = "update_date" )
    @Temporal( TemporalType.TIMESTAMP )
    private java.util.Date updateDate;

    @Column( name = "attempts" )
    private int attempts;

    @Column( name = "total_requests" )
    private int totalRequests;

    @Column( name = "completed_requests" )
    private int completedRequests;


    public CommandRequest()
    {
        this.createDate = new java.util.Date();
        this.updateDate = new java.util.Date();
    }


    public String getCommandId()
    {
        return commandId;
    }


    public void setCommandId( String commandId )
    {
        this.commandId = commandId;
    }


    public String getSourcePeerId()
    {
        return sourcePeerId;
    }


    public void setSourcePeerId( String sourcePeerId )
    {
        this.sourcePeerId = sourcePeerId;
    }


    public java.util.Date getCreateDate()
    {
        return createDate;
    }


    public void setCreateDate( java.util.Date createDate )
    {
        this.createDate = createDate;
    }


    public java.util.Date getUpdateDate()
    {
        return updateDate;
    }


    public void setUpdateDate( java.util.Date updateDate )
    {
        this.updateDate = updateDate;
    }


    public int getAttempts()
    {
        return attempts;
    }


    public void setAttempts( int attempts )
    {
        this.attempts = attempts;
    }


    public int getTotalRequests()
    {
        return totalRequests;
    }


    public void setTotalRequests( int totalRequests )
    {
        this.totalRequests = totalRequests;
    }


    public int getCompletedRequests()
    {
        return completedRequests;
    }


    public void setCompletedRequests( int completedRequests )
    {
        this.completedRequests = completedRequests;
    }
}
