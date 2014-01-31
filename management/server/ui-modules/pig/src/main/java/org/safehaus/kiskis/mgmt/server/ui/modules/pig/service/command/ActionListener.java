package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.command;

import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Context;

public interface ActionListener {

    public void onExecute(Context context);

    public void onResponse(Context context, String stdOut, String stdErr, boolean isError);

}
