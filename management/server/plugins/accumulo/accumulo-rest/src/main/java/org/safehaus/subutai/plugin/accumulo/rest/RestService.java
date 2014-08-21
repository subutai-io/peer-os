package org.safehaus.subutai.plugin.accumulo.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;


//Maps for the `say` in the URL
@Path ("say")
public interface RestService {
	@GET
	@Path ("hello/{name}") //Maps for the `hello/John` in the URL
	public String handleGet(@PathParam ("name") String name);
}