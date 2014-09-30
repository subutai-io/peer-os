package org.safehaus.subutai.core.db.ext.api.entity;


import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Table;

//********************************************************

@Entity @IdClass(CommandResponseId.class)
@Table( name = "command_response" )

public class CommandResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @Column( name = "command_id" )
    private String commandId;
    
    @Id
    @Column( name = "agent_id" )
    private String agentId;
    
    @Id
    @Column( name = "response_number") 
    private int responseNumber;
   
    @Column( name = "create_date" )
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createDate;
   
    @Column( name = "status" )
    private short status;

    @Column( name = "payload" )
    private String payload;
    
    
    public CommandResponse()
    {
        this.createDate = new java.util.Date();
    }


    public String getCommandId() {
        return commandId;
    }



    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }



    public String getAgentId() {
        return agentId;
    }



    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public int getResponseNumber() {
        return responseNumber;
    }


    public void setResponseNumber(int responseNumber) {
        this.responseNumber = responseNumber;
    }

    public java.util.Date getCreateDate() {
        return createDate;
    }


    public void setCreateDate(java.util.Date createDate) {
        this.createDate = createDate;
    }


    public short getStatus() {
        return status;
    }


    public void setStatus(short status) {
        this.status = status;
    }


    public String getPayload() {
        return payload;
    }


    public void setPayload(String payload) {
        this.payload = payload;
    }
    
   
    
}


