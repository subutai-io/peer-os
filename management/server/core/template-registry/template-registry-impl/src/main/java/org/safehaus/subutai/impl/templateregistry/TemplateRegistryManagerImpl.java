/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.templateregistry;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.api.templateregistry.TemplateTree;
import org.safehaus.subutai.shared.protocol.settings.Common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This is an implementation of TemplateRegistryManager
 */
public class TemplateRegistryManagerImpl implements TemplateRegistryManager {

	private final TemplateDAO templateDAO;


	public TemplateRegistryManagerImpl(final DbManager dbManager) {
		Preconditions.checkNotNull(dbManager, "DB Manager is null");
		templateDAO = new TemplateDAO(dbManager);
	}


	@Override
	public void registerTemplate(final String configFile, final String packagesFile) {

		Preconditions.checkArgument(!Strings.isNullOrEmpty(configFile), "Config file contents is null or empty");
		Preconditions
				.checkArgument(!Strings.isNullOrEmpty(packagesFile), "Packages file contents is null or empty");

		Template template = parseTemplate(configFile, packagesFile);

		//save template to storage
		if (!templateDAO.saveTemplate(template)) {
			throw new RuntimeException(String.format("Error registering template %s", template.getTemplateName()));
		}
	}


	//@todo parse packages manifest to create packages list
	private Template parseTemplate(String configFile, String packagesFile) {
		Properties properties = new Properties();
		try {
			properties.load(new ByteArrayInputStream(configFile.getBytes()));
			String lxcUtsname = properties.getProperty("lxc.utsname");
			String lxcArch = properties.getProperty("lxc.arch");
			String subutaiConfigPath = properties.getProperty("subutai.config.path");
			String subutaiParent = properties.getProperty("subutai.parent");
			String subutaiGitBranch = properties.getProperty("subutai.git.branch");
			String subutaiGitUuid = properties.getProperty("subutai.git.uuid");

			Template template = new Template(lxcArch, lxcUtsname, subutaiConfigPath, subutaiParent, subutaiGitBranch,
					subutaiGitUuid, packagesFile);

			if (template.getParentTemplateName() == null) {

				template.setProducts(getPackagesDiff(null, template));
			} else {
				Template parentTemplate = getTemplate(template.getParentTemplateName());

				template.setProducts(getPackagesDiff(parentTemplate, template));
			}


			return template;
		} catch (IOException e) {
			throw new RuntimeException(String.format("Error parsing template configuration %s", e));
		}
	}


	private Set<String> getPackagesDiff(Template parent, Template child) {
		if (parent == null) {
			return extractPackageNames(Sets.newHashSet(child.getPackagesManifest().split("\n")));
		} else {
			return getPackagesDiff(Sets.newHashSet(parent.getPackagesManifest().split("\n")),
					Sets.newHashSet(child.getPackagesManifest().split("\n")));
		}
	}

	private Set<String> extractPackageNames(Set<String> ls) {
		Pattern p = Pattern.compile(String.format("(%s.+?)\\s", Common.PACKAGE_PREFIX));
		Matcher m = p.matcher("");

		SortedSet<String> res = new TreeSet<>();
		for (String s : ls) {
			if (m.reset(s).find()) {
				res.add(m.group(1));
			}
		}

		return res;
	}

	private Set<String> getPackagesDiff(Set<String> parentPackages, Set<String> childPackages) {
		Set<String> p = extractPackageNames(parentPackages);
		Set<String> c = extractPackageNames(childPackages);
		c.removeAll(p);

		return c;
	}

	@Override
	public void unregisterTemplate(final String templateName) {
		unregisterTemplate(templateName, Common.DEFAULT_LXC_ARCH);
	}


	@Override
	public void unregisterTemplate(final String templateName, final String lxcArch) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(templateName), "Template name is null or empty");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(lxcArch), "LxcArch is null or empty");
		//delete template from storage
		Template template = getTemplate(templateName, lxcArch);
		if (template != null) {
			if (!templateDAO.removeTemplate(template)) {
				throw new RuntimeException(String.format("Error unregistering template %s", templateName));
			}
		} else {
			throw new RuntimeException(String.format("Template %s not found", templateName));
		}
	}


	@Override
	public Template getTemplate(final String templateName) {
		return getTemplate(templateName, Common.DEFAULT_LXC_ARCH);
	}


	@Override
	public Template getTemplate(final String templateName, String lxcArch) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(templateName), "Template name is null or empty");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(lxcArch), "LxcArch is null or empty");
		//retrieve template from storage

		return templateDAO.getTemplateByName(templateName, lxcArch);
	}


	@Override
	public List<Template> getChildTemplates(final String parentTemplateName) {
		return getChildTemplates(parentTemplateName, Common.DEFAULT_LXC_ARCH);
	}


	@Override
	public List<Template> getChildTemplates(final String parentTemplateName, String lxcArch) {
		Preconditions
				.checkArgument(!Strings.isNullOrEmpty(parentTemplateName), "Parent template name is null or empty");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(lxcArch), "LxcArch is null or empty");
		//retrieve child templates from storage
		return templateDAO.geChildTemplates(parentTemplateName, lxcArch);
	}


	@Override
	public Template getParentTemplate(final String childTemplateName) {
		return getParentTemplate(childTemplateName, Common.DEFAULT_LXC_ARCH);
	}


	@Override
	public Template getParentTemplate(final String childTemplateName, final String lxcArch) {
		Preconditions
				.checkArgument(!Strings.isNullOrEmpty(childTemplateName), "Child template name is null or empty");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(lxcArch), "LxcArch is null or empty");
		//retrieve parent template from storage
		Template child = getTemplate(childTemplateName, lxcArch);

		if (child != null) {
			if (!Strings.isNullOrEmpty(child.getParentTemplateName())) {
				return getTemplate(child.getParentTemplateName(), child.getLxcArch());
			}
		}

		return null;
	}


	@Override
	public TemplateTree getTemplateTree() {
		//retrieve all templates and fill template tree
		TemplateTree templateTree = new TemplateTree();
		List<Template> allTemplates = templateDAO.getAllTemplates();
		for (Template template : allTemplates) {
			templateTree.addTemplate(template);
		}
		return templateTree;
	}


	@Override
	public List<Template> getParentTemplates(String childTemplateName) {
		return getParentTemplates(childTemplateName, Common.DEFAULT_LXC_ARCH);
	}


	@Override
	public List<Template> getParentTemplates(final String childTemplateName, final String lxcArch) {
		List<Template> parents = new ArrayList<>();

		Template parent = getParentTemplate(childTemplateName, lxcArch);
		while (parent != null) {
			parents.add(parent);
			parent = getParentTemplate(parent.getTemplateName(), lxcArch);
		}
		Collections.reverse(parents);
		return parents;
	}


	@Override
	public List<Template> getAllTemplates() {
		return getAllTemplates(Common.DEFAULT_LXC_ARCH);
	}


	@Override
	public List<Template> getAllTemplates(final String lxcArch) {
		List<Template> allTemplates = templateDAO.getAllTemplates();
		List<Template> result = new ArrayList<>();

		for (Template template : allTemplates) {
			if (template.getLxcArch().equalsIgnoreCase(lxcArch)) {
				result.add(template);
			}
		}
		return result;
	}
}
