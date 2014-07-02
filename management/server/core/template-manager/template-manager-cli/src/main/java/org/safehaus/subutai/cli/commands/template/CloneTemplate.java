package org.safehaus.subutai.cli.commands.template;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.manager.TemplateManager;

@Command(scope = "template-man", name = "clone", description = "clone new instance")
public class CloneTemplate extends OsgiCommandSupport {

    private TemplateManager templateManager;

    @Argument(index = 0, required = true)
    private String hostName;
    @Argument(index = 1, required = true)
    private String templateName;
    @Argument(index = 3, required = true)
    private String cloneName;

    public TemplateManager getTemplateManaget() {
        return templateManager;
    }

    public void setTemplateManaget(TemplateManager templateManaget) {
        this.templateManager = templateManaget;
    }

    @Override
    protected Object doExecute() throws Exception {
        boolean b = templateManager.clone(hostName, templateName, cloneName);
        if(b)
            System.out.println(String.format("New instance '%s' is clone in %s", cloneName, hostName));
        else
            System.out.println("Failed to clone new instance");
        return null;
    }

}
