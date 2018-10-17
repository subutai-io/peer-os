package io.subutai.bazaar.share.dto.ansible;


import java.util.HashSet;
import java.util.Set;


public class AnsibleDto
{
    public enum State
    {
        FAILED, SUCCESS, IN_PROGRESS
    }

    private String ansibleContainerId;
    private String repoLink;
    private String ansibleRootFile;
    private State state;
    private Set<Group> groups = new HashSet();
    private String logs;
    private String vars;
    private Long commandTimeout; //this is for RequestBuilder timeout



    public AnsibleDto()
    {
    }


    public String getAnsibleContainerId()
    {
        return ansibleContainerId;
    }


    public void setAnsibleContainerId( final String ansibleContainerId )
    {
        this.ansibleContainerId = ansibleContainerId;
    }


    public String getRepoLink()
    {
        return repoLink;
    }


    public void setRepoLink( final String repoLink )
    {
        this.repoLink = repoLink;
    }


    public String getAnsibleRootFile()
    {
        return ansibleRootFile;
    }


    public void setAnsibleRootFile( final String ansibleRootFile )
    {
        this.ansibleRootFile = ansibleRootFile;
    }


    public State getState()
    {
        return state;
    }


    public void setState( final State state )
    {
        this.state = state;
    }


    public String getLogs()
    {
        return logs;
    }


    public void setLogs( final String logs )
    {
        this.logs = logs;
    }


    public Set<Group> getGroups()
    {
        return groups;
    }


    public void setGroups( final Set<Group> groups )
    {
        this.groups = groups;
    }


    public String getVars()
    {
        return vars;
    }


    public void setVars( final String vars )
    {
        this.vars = vars;
    }

    public Long getCommandTimeout()
    {
        return commandTimeout;
    }


    public void setCommandTimeout( final Long commandTimeout )
    {
        this.commandTimeout = commandTimeout;
    }
}
