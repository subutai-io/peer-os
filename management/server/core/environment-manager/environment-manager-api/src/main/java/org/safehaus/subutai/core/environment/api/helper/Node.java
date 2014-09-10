package org.safehaus.subutai.core.environment.api.helper;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.registry.api.Template;


/**
 * Created by bahadyr on 7/24/14.
 */
public class Node {

    private Agent agent;
    private Template template;
    private String nodeGroupName;


    public Node( final Agent agent, final Template template, final String nodeGroupName ) {
        this.agent = agent;
        this.template = template;
        this.nodeGroupName = nodeGroupName;
    }


    public String getNodeGroupName() {
        return nodeGroupName;
    }


    public Agent getAgent() {
        return agent;
    }


    public Template getTemplate() {
        return template;
    }


    @Override
    public String toString() {
        return "Node{" +
                "agent=" + agent +
                ", template=" + template +
                ", nodeGroupName='" + nodeGroupName + '\'' +
                '}';
    }
}
