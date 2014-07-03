package org.safehaus.subutai.api.manager.helper;


import java.util.List;

import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 6/24/14.
 */
public class EnvironmentNodeGroup {


    private String templateUsed;
    private List<Agent> instances;


    public String getTemplateUsed() {
        return templateUsed;
    }


    public void setTemplateUsed( final String templateUsed ) {
        this.templateUsed = templateUsed;
    }


    public List<Agent> getInstances() {
        return instances;
    }


    public void setInstances( final List<Agent> instances ) {
        this.instances = instances;
    }
}
