/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import com.google.gson.Gson;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandInterface;

/**
 * @author dilshat
 */
public class CommandJson {

    private static Gson gson = new Gson();

    public static Request getRequest(String json) {
        try {
            Command cmd = gson.fromJson(json, Command.class);
            if (cmd.command != null) {
                return (Request) cmd.command;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    public static Response getResponse(String json) {
        try {
            Command cmd = gson.fromJson(json, Command.class);
            if (cmd.response != null) {
                return (Response) cmd.response;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getJson(CommandInterface cmd) {
        try {

            return gson.toJson(cmd);
        } catch (Exception e) {
        }
        return null;
    }
//    public static String getJson(Request request) {
//        return gson.toJson(request);
//    }
}
