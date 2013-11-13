package org.safehaus.kiskis.mgmt.shared.protocol.api.ui;

import org.safehaus.kiskis.mgmt.shared.protocol.Response;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/12/13
 * Time: 7:17 PM
 */
public interface CommandListener {
    public void outputCommand(Response response);

    public String getName();
}
