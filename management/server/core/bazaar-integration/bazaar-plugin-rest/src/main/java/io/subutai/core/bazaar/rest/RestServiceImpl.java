package io.subutai.core.bazaar.rest;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.bazaar.api.Bazaar;
import io.subutai.core.hubmanager.api.HubPluginException;

public class RestServiceImpl implements RestService
{
	private Bazaar bazaar;
	private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );

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
	public Response installPlugin (String name, String version, String kar, String url, String uid)
	{
		try
		{
			bazaar.installPlugin (name, version, kar, url, uid);
		}
		catch (HubPluginException e)
		{
			e.printStackTrace();
			return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
		}
		return Response.status (Response.Status.OK).build();
	}

	@Override
	public Response uninstallPlugin (Long id, String name)
	{
		bazaar.uninstallPlugin (id, name);
		return Response.status (Response.Status.OK).build();
	}

	@Override
	public Response restorePlugin (Long id, String name, String version, String kar, String url, String uid)
	{
		try
		{
			bazaar.restorePlugin (id, name, version, kar, url, uid);
		}
		catch (HubPluginException e)
		{
			e.printStackTrace();
			return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
		}
		return Response.status (Response.Status.OK).build();
	}

	@Override
	public Response getListMD5 ()
	{
		return Response.status( Response.Status.OK ).entity( JsonUtil.toJson( bazaar.getChecksum() ) ).build();
	}

	public void setBazaar (final Bazaar bazaar)
	{
		this.bazaar = bazaar;
	}
}
