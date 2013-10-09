package org.safehaus.kiskis.mgmt.shared.communication.interfaces.impl;

import com.google.gson.Gson;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.IRequestGenerator;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 10/8/13
 * Time: 7:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class RequestGenerator implements IRequestGenerator {

    private Gson gson = new Gson();

    @Override
    public Request fromJson(String json) {
        return gson.fromJson(json, Request.class);
    }

    @Override
    public String toJson(Request request) {
        return gson.toJson(request, Request.class);
    }
}
