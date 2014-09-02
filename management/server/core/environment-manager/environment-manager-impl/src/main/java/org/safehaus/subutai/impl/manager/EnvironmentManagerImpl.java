/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.manager;


import java.util.List;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.manager.EnvironmentManager;
import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.exception.EnvironmentDestroyException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.network.NetworkManager;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.impl.manager.builder.EnvironmentBuilder;
import org.safehaus.subutai.impl.manager.dao.EnvironmentDAO;
import org.safehaus.subutai.impl.manager.util.BlueprintParser;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;

import com.google.common.base.Strings;


/**
 * This is an implementation of EnvironmentManager
 */
public class EnvironmentManagerImpl implements EnvironmentManager {

	private EnvironmentDAO environmentDAO;
	private EnvironmentBuilder environmentBuilder;
	private BlueprintParser blueprintParser;
	private ContainerManager containerManager;


	public EnvironmentManagerImpl(final ContainerManager containerManager,
	                              final TemplateRegistryManager templateRegistryManager, final DbManager dbManager,
	                              final AgentManager agentManager, final NetworkManager networkManager) {
		this.containerManager = containerManager;
		this.environmentDAO = new EnvironmentDAO(dbManager);
		this.blueprintParser = new BlueprintParser();
		environmentBuilder = new EnvironmentBuilder(templateRegistryManager, agentManager, networkManager);
	}


	/**
	 * Builds an environment by provided blueprint description
	 */
	@Override
	public boolean buildEnvironment(String blueprintStr) {

		EnvironmentBlueprint blueprint = blueprintParser.parseEnvironmentBlueprintText(blueprintStr);
		return build(blueprint);
	}


	public boolean buildEnvironment(EnvironmentBlueprint blueprint) {
		return build(blueprint);
	}


	@Override
	public Environment buildEnvironmentAndReturn(final EnvironmentBlueprint blueprint)
			throws EnvironmentBuildException {

		return environmentBuilder.build(blueprint, containerManager);
	}

	@Override
	public List<Environment> getEnvironments() {
		return environmentDAO.getEnvironments();
	}

	@Override
	public Environment getEnvironmentInfo(final String environmentName) {
		return environmentDAO.getEnvironment(environmentName);
	}

	@Override
	public boolean destroyEnvironment(final String environmentName) {
		Environment environment = getEnvironmentInfo(environmentName);
		try {
			environmentBuilder.destroy(environment);
			//TODO environmentDAO.deleteEnvironmentInfo( environment.getName() );
			return true;
		} catch (EnvironmentDestroyException e) {

			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean saveBlueprint(String blueprintStr) {
		EnvironmentBlueprint blueprint = blueprintParser.parseEnvironmentBlueprintText(blueprintStr);
		return environmentDAO.saveBlueprint(blueprint);
	}

	@Override
	public List<EnvironmentBlueprint> getBlueprints() {
		return environmentDAO.getBlueprints();
	}

	@Override
	public boolean deleteBlueprint(String blueprintName) {
		return environmentDAO.deleteBlueprint(blueprintName);
	}

	@Override
	public String parseBlueprint(final EnvironmentBlueprint blueprint) {
		return blueprintParser.parseEnvironmentBlueprint(blueprint);
	}

	private boolean build(EnvironmentBlueprint blueprint) {
		if (blueprint != null && !Strings.isNullOrEmpty(blueprint.getName())) {
			try {
				Environment environment = environmentBuilder.build(blueprint, containerManager);
				boolean saveResult = environmentDAO.saveEnvironment(environment);
				if (!saveResult) {
					//rollback build action.
					try {
						environmentBuilder.destroy(environment);
					} catch (EnvironmentDestroyException ignore) {
					}
					return false;
				}
				return true;
			} catch (EnvironmentBuildException e) {
				System.out.println(e.getMessage());
			}
		}
		return false;
	}
}
