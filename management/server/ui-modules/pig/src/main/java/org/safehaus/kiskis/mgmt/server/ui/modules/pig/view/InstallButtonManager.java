package org.safehaus.kiskis.mgmt.server.ui.modules.pig.view;

import com.vaadin.ui.Button;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.common.command.CommandAction;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.ContextUtil;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.install.CheckListener;
import org.safehaus.kiskis.mgmt.server.ui.modules.pig.service.install.InstallListener;

public class InstallButtonManager {

    static Button getButton(final UILog uiLog) {

        Button button = new Button("Install");

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

//                "--force-yes",
//                "--assume-yes",
//                "install",
//                "ksks-cassandra"

        CommandAction installAction = new CommandAction("apt-get --force-yes --assume-yes install ksks-pig; dpkg -l|grep ksks;", new InstallListener(uiLog));

        Chain chain = new Chain(checkAction, installAction);
        chain.execute(ContextUtil.create());
    }

}
