package org.safehaus.subutai.cli.templateregistry;


import com.google.common.base.Strings;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;

import java.util.List;


/**
 * CLI for TemplateRegistryManager.getChildTemplates command
 */
@Command (scope = "registry", name = "get-child-templates",
		description = "Get child templates by parent template name")
public class GetChildTemplatesCommand extends OsgiCommandSupport {
	@Argument (index = 0, name = "parent template name", required = true, multiValued = false,
			description = "parent template name")
	String parentTemplateName;
	@Argument (index = 1, name = "lxc arch", required = false, multiValued = false,
			description = "lxc arch, default = amd64")
	String lxcArch;

	private TemplateRegistryManager templateRegistryManager;


	public void setTemplateRegistryManager(final TemplateRegistryManager templateRegistryManager) {
		this.templateRegistryManager = templateRegistryManager;
	}


	@Override
	protected Object doExecute() throws Exception {
		List<Template> templates =
				Strings.isNullOrEmpty(lxcArch) ? templateRegistryManager.getChildTemplates(parentTemplateName) :
						templateRegistryManager.getChildTemplates(parentTemplateName, lxcArch);

		if (templates != null && !templates.isEmpty()) {
			for (Template template : templates) {
				System.out.println(template + "\n");
			}
		} else {
			System.out.println(String.format("Child templates of %s not found", parentTemplateName));
		}

		return null;
	}
}
