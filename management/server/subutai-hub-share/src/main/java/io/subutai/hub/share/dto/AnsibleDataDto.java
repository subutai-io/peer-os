package io.subutai.hub.share.dto;


import java.util.List;


public class AnsibleDataDto
{
    public enum Action
    {
        CONFIGURE, GROW, SHRINK, DESTROY
    }


    private Action action;

    private String ansibleTemplateId;

    private String playbook;

    private List<String> containers;


    public Action getAction()
    {
        return action;
    }


    public void setAction( final Action action )
    {
        this.action = action;
    }


    public String getAnsibleTemplateId()
    {
        return ansibleTemplateId;
    }


    public void setAnsibleTemplateId( final String ansibleTemplateId )
    {
        this.ansibleTemplateId = ansibleTemplateId;
    }


    public String getPlaybook()
    {
        return playbook;
    }


    public void setPlaybook( final String playbook )
    {
        this.playbook = playbook;
    }


    public List<String> getContainers()
    {
        return containers;
    }


    public void setContainers( final List<String> containers )
    {
        this.containers = containers;
    }
}
