package org.safehaus.subutai.ui.containermanager.clone;

/**
 * Created by timur on 9/8/14.
 */
public abstract class AbstractCommand {
    protected String hostName;
    protected String templateName;
    protected String cloneName;

    public AbstractCommand(String hostname, String templateName, String cloneName) {
        this.hostName = hostname;
        this.templateName = templateName;
        this.cloneName = cloneName;
    }
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getCloneName() {
        return cloneName;
    }

    public void setCloneName(String cloneName) {
        this.cloneName = cloneName;
    }
}
