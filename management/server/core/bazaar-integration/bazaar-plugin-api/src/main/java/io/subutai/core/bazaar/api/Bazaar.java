package io.subutai.core.bazaar.api;


import java.util.List;

import io.subutai.core.bazaar.api.model.Plugin;
import io.subutai.core.hubmanager.api.HubPluginException;

public interface Bazaar
{
	String getProducts();

	List <Plugin> getPlugins();

	void installPlugin (String name, String version, String kar, String url, String uid) throws HubPluginException;

	void uninstallPlugin (Long id, String kar);
}
