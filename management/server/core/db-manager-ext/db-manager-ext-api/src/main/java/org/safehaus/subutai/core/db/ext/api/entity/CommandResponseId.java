package org.safehaus.subutai.core.db.ext.api.entity;


import java.io.Serializable;


/**
 * Created by nurkaly on 9/30/14.
 */
public class CommandResponseId  implements Serializable {

    private String commandId;
    private String agentId;
    private int    responseNumber;


    public String getCommandId() {
        return commandId;
    }


    public void setCommandId( final String commandId ) {
        this.commandId = commandId;
    }


    public String getAgentId() {
        return agentId;
    }


    public void setAgentId( final String agentId ) {
        this.agentId = agentId;
    }


    public int getResponseNumber() {
        return responseNumber;
    }


    public void setResponseNumber( final int responseNumber ) {
        this.responseNumber = responseNumber;
    }
}