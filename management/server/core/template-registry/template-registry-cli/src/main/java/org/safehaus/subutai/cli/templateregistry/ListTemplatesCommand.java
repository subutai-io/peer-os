package org.safehaus.subutai.cli.templateregistry;


import com.google.common.base.Strings;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;

import java.util.List;


/**
 * CLI for TemplateRegistryManager.listTemplates command
 */
@Command (scope = "registry", name = "list-templates", description = "List templates")
public class ListTemplatesCommand extends OsgiCommandSupport {
	@Argument (index = 0, name = "lxc arch", required = false, multiValued = false,
			description = "lxc arch, default = amd64")
	String lxcArch;

	private TemplateRegistryManager templateRegistryManager;


	public void setTemplateRegistryManager(final TemplateRegistryManager templateRegistryManager) {
		this.templateRegistryManager = templateRegistryManager;
	}


	@Override
	protected Object doExecute() throws Exception {

		List<Template> templates = Strings.isNullOrEmpty(lxcArch) ? templateRegistryManager.getAllTemplates() :
				templateRegistryManager.getAllTemplates(lxcArch);


		for (Template template : templates) {
			System.out.println(String.format("%s %s", template.getTemplateName(),
					Strings.isNullOrEmpty(template.getParentTemplateName()) ? "" :
							template.getParentTemplateName()));
		}

		return null;
	}
}
