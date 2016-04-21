package io.subutai.core.bazaar.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface RestService
{
	@GET
	@Path( "products/checksum" )
	@Produces( { MediaType.APPLICATION_JSON } )
	public Response getListMD5 ();


	@GET
	@Path( "products" )
	@Produces( { MediaType.APPLICATION_JSON } )
	public Response listProducts ();

	@GET
	@Path( "installed" )
	@Produces( { MediaType.APPLICATION_JSON } )
	public Response listInstalled ();

	@POST
	@Path( "install" )
	@Produces( { MediaType.TEXT_PLAIN } )
	public Response installPlugin (@FormParam ("name") String name, @FormParam ("version") String version, @FormParam ("kar") String kar, @FormParam ("url") String url, @FormParam ("uid") String uid);

	@POST
	@Path( "uninstall" )
	@Produces( { MediaType.TEXT_PLAIN } )
	public Response uninstallPlugin (@FormParam ("id") Long id, @FormParam ("name") String name);


	@POST
	@Path( "restore" )
	@Produces( { MediaType.TEXT_PLAIN } )
	public Response restorePlugin (@FormParam ("id") Long id, @FormParam ("name") String name, @FormParam ("version") String version, @FormParam ("kar") String kar, @FormParam ("url") String url, @FormParam ("uid") String uid);
}