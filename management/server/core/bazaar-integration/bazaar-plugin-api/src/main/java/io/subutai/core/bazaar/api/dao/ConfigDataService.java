package io.subutai.core.bazaar.api.dao;


import io.subutai.core.bazaar.api.model.Plugin;

import java.util.List;

public interface ConfigDataService
{
	void savePlugin (final String name, final String version, final String kar, final String url, final String uid);

	void deletePlugin (final Long id);

	List<Plugin> getPlugins();
}
