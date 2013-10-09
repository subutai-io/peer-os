package org.safehaus.kiskis.mgmt.shared.communication.interfaces.impl;

import com.google.gson.Gson;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.IGenerator;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 10/8/13
 * Time: 7:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandGenerator implements IGenerator {

    private Gson gson = new Gson();

    @Override
    public Command fromJson(String json) {
        return gson.fromJson(json, Command.class);
    }

    @Override
    public String toJson(Command request) {
        return gson.toJson(request, Command.class);
    }
}
