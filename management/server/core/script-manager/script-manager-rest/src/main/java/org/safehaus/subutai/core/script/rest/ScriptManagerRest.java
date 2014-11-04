package org.safehaus.subutai.core.script.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;


/**
 * Interface for Script Manager REST
 */
public interface ScriptManagerRest
{
    @POST
    @Path( "/" )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response uploadFile( @Multipart( "script" ) Attachment attachment );
}
