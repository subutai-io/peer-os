package io.subutai.core.bazaar.api;


import io.subutai.core.bazaar.api.model.Plugin;
import io.subutai.plugin.hub.api.HubPluginException;

import java.util.List;

public interface Bazaar
{
	String getProducts();

	List <Plugin> getPlugins();

	void installPlugin (String name, String version, String kar, String url) throws HubPluginException;

	void uninstallPlugin (Long id, String kar);
}
