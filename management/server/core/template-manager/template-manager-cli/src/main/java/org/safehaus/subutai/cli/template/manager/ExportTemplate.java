package org.safehaus.subutai.cli.template.manager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.template.manager.TemplateManager;

@Command (scope = "template", name = "export", description = "export template")
public class ExportTemplate extends OsgiCommandSupport {

	private TemplateManager templateManager;

	@Argument (index = 0, required = true)
	private String hostName;
	@Argument (index = 1, required = true)
	private String templateName;

	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	@Override
	protected Object doExecute() throws Exception {
		String path = templateManager.exportTemplate(hostName, templateName);
		if (path != null) System.out.println("Template successfully exported to " + path);
		else System.out.println("Failed to export");
		return null;
	}

}
