package org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain;

import java.util.*;

public class Chain {

    private final List<Action> ACTIONS = new ArrayList<Action>();
    private int i = -1;

    public Chain(Action ... actions) {
        for (Action action : actions) {
            ACTIONS.add(action);
        }
    }

    public void add(Action action) {
        ACTIONS.add(action);
    }

    public void add(Chain chain) {
        ACTIONS.addAll(chain.ACTIONS);
    }

    public void execute(Context context) {
        i++;
        if (i < ACTIONS.size()) {
            ACTIONS.get(i).execute(context, this);
        }
    }
}
