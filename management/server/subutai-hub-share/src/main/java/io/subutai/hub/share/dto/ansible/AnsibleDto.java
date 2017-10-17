package io.subutai.hub.share.dto.ansible;


import java.util.HashSet;
import java.util.Set;


public class AnsibleDto
{
    public enum State
    {
        FAILED, SUCCESS
    }

    private String ansibleContainerId;
    private String repoLink;
    private String ansibleRootFile;
    private Set<Host> hosts = new HashSet<>();
    private State state;
    private String logs;


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


    public Set<Host> getHosts()
    {
        return hosts;
    }


    public void setHosts( final Set<Host> hosts )
    {
        this.hosts = hosts;
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
}
