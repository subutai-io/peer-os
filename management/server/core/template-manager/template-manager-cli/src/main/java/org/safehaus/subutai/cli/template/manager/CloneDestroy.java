package org.safehaus.subutai.cli.template.manager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.template.manager.TemplateManager;

@Command (scope = "template", name = "destroy", description = "destroy clone")
public class CloneDestroy extends OsgiCommandSupport {

	private TemplateManager templateManager;

	@Argument (index = 0, required = true)
	private String hostName;
	@Argument (index = 1, required = true)
	private String cloneName;

	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	@Override
	protected Object doExecute() throws Exception {
		boolean b = templateManager.cloneDestroy(hostName, cloneName);
		if (b) System.out.println("Clone successfully destroyed");
		else System.out.println("Failed to destroy");
		return null;
	}

}
