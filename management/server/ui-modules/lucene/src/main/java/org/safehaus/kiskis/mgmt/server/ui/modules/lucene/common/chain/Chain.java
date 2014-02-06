package org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.chain;

import java.util.ArrayList;
import java.util.List;

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

    public void start(Context context) {
        i = -1;
        proceed(context);
    }

    // TODO exception handling
    public void proceed(Context context) {
        i++;
        if (i < ACTIONS.size()) {
            ACTIONS.get(i).execute(context, this);
        }
    }
}
