/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.safehaus.kiskis.mgmt.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

//Maps for the `say` in the URL

@Path("say")
public interface HelloRestService {

    @GET
    @Path("hello/{name}") //Maps for the `hello/John` in the URL
    public String handleGet(@PathParam("name") String name);

}