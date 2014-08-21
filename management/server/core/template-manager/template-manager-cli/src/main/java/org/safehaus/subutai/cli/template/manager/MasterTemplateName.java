package org.safehaus.subutai.cli.template.manager;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.template.manager.TemplateManager;

@Command (scope = "template", name = "get-master-template-name", description = "get master template name")
public class MasterTemplateName extends OsgiCommandSupport {

	private TemplateManager templateManager;

	public TemplateManager getTemplateManager() {
		return templateManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	@Override
	protected Object doExecute() throws Exception {
		System.out.println(templateManager.getMasterTemplateName());
		return null;
	}

}
