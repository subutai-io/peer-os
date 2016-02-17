package io.subutai.core.bazaar.rest;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.bazaar.api.Bazaar;
import io.subutai.plugin.hub.api.HubPluginException;

import javax.ws.rs.core.Response;

public class RestServiceImpl implements RestService
{
	private Bazaar bazaar;

	@Override
	public Response listProducts ()
	{
		return Response.status( Response.Status.OK ).entity( bazaar.getProducts () ).build();
	}

	@Override
	public Response listInstalled ()
	{
		return Response.status (Response.Status.OK).entity (JsonUtil.toJson(bazaar.getPlugins ())).build ();
	}

	@Override
	public Response installPlugin (String name, String version, String kar, String url)
	{
		try
		{
			bazaar.installPlugin (name, version, kar, url);
		}
		catch (HubPluginException e)
		{
			e.printStackTrace();
			return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
		}
		return Response.status (Response.Status.OK).build();
	}

	@Override
	public Response uninstallPlugin (Long id, String kar)
	{
		bazaar.uninstallPlugin (id, kar);
		return Response.status (Response.Status.OK).build();
	}

	public void setBazaar (final Bazaar bazaar)
	{
		this.bazaar = bazaar;
	}
}
