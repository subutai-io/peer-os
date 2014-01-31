package org.safehaus.kiskis.mgmt.server.ui.modules.pig.view;

import com.vaadin.ui.Button;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.ContextUtil;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.status.CheckStatusListener;

public class StatusButtonManager {

    static Button getButton(final UILog uiLog) {

        Button button = new Button("Check Status");

        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                onClick(uiLog);
            }
        });

        return button;
    }

    private static void onClick(UILog uiLog) {

        CommandAction commandAction = new CommandAction("dpkg -l|grep ksks", new CheckStatusListener(uiLog));

        Chain chain = new Chain(commandAction);
        chain.execute(ContextUtil.create());
    }

}
