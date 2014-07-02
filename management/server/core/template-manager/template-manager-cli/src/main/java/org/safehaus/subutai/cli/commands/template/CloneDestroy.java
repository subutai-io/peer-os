package org.safehaus.subutai.cli.commands.template;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.manager.TemplateManager;

@Command(scope = "template-man", name = "clone-destroy", description = "destroy clone")
public class CloneDestroy extends OsgiCommandSupport {

    private TemplateManager templateManager;

    @Argument(index = 0, required = true)
    private String hostName;
    @Argument(index = 1, required = true)
    private String cloneName;

    public TemplateManager getTemplateManaget() {
        return templateManager;
    }

    public void setTemplateManaget(TemplateManager templateManaget) {
        this.templateManager = templateManaget;
    }

    @Override
    protected Object doExecute() throws Exception {
        boolean b = templateManager.cloneDestroy(hostName, cloneName);
        if(b) System.out.println("Clone successfully destroyed");
        else System.out.println("Failed to destroy");
        return null;
    }

}
