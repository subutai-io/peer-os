package org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain;

import java.util.Map;

public interface Action {

    void execute(Map<String, Object> context, Chain chain);

}
