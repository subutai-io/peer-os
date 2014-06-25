package org.safehaus.subutai.api.manager.helper;


import java.util.Set;


/**
 * Created by bahadyr on 6/24/14.
 */
public class EnvironmentNodeGroup {


    private String templateUsed;


    public String getTemplateUsed() {
        return templateUsed;
    }


    public void setTemplateUsed( final String templateUsed ) {
        this.templateUsed = templateUsed;
    }


    Set<EnvironmentGroupInstance> environmentGroupInstanceSet;


    public Set<EnvironmentGroupInstance> getEnvironmentGroupInstanceSet() {
        return environmentGroupInstanceSet;
    }


    public void setEnvironmentGroupInstanceSet( final Set<EnvironmentGroupInstance> environmentGroupInstanceSet ) {
        this.environmentGroupInstanceSet = environmentGroupInstanceSet;
    }
}
