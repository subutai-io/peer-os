package org.safehaus.kiskis.mgmt.shared.protocol.impl;

import com.google.gson.Gson;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.IResponseGenerator;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 10/8/13
 * Time: 7:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResponseGenerator implements IResponseGenerator {

    Gson gson = new Gson();

    @Override
    public Response fromJson(String json) {
        return gson.fromJson(json, Response.class);
    }

    @Override
    public String toJson(Response request) {
        return gson.toJson(request, Response.class);
    }
}
