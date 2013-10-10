package org.safehaus.kiskis.mgmt.shared.communication.util;

import com.google.gson.Gson;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Response;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 10/8/13
 * Time: 7:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class JsonGenerator{

    private static Gson gson;

    static {
        gson = new Gson();
    }

    public static Response fromJson(String json) {
        return gson.fromJson(json, Response.class);
    }

    public static String toJson(Request request) {
        return gson.toJson(request, Request.class);
    }
}
