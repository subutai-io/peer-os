package org.safehaus.kiskis.mgmt.server.ui.modules.pig.view;

import com.vaadin.ui.Button;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.ContextUtil;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.remove.*;

public class RemoveButtonManager {

    static Button getButton(final UILog uiLog) {

        Button button = new Button("Remove");

        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                onClick(uiLog);
            }
        });

        return button;
    }

    private static void onClick(UILog uiLog) {

        CommandAction checkAction = new CommandAction("dpkg -l|grep ksks", new CheckListener(uiLog));

        CommandAction removeAction = new CommandAction("apt-get --force-yes --assume-yes --purge remove ksks-pig", new RemoveListener(uiLog));

        Chain chain = new Chain(checkAction, removeAction);
        chain.execute(ContextUtil.create());
    }

}
