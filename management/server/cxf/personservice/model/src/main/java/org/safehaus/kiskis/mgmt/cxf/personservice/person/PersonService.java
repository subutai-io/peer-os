package org.safehaus.kiskis.mgmt.cxf.personservice.person;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_XML)
@WebService
public interface PersonService {
    @GET
    @Path("/")
    public Person[] getAll();
    
    @GET
    @Path("/{id}")
    public Person getPerson(String id);
    
    @PUT
    @Path("/{id}")
    public void updatePerson(String id, Person person);
    
    @POST
    @Path("/")
    public void addPerson(Person person);
}
