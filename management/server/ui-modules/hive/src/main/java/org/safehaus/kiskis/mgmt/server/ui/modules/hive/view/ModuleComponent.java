package org.safehaus.kiskis.mgmt.server.ui.modules.hive.view;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.action.chain.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.chain.Chain;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.CommandBuilder;
import org.safehaus.kiskis.mgmt.server.ui.modules.hive.common.command.CommandExecutor;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

public class ModuleComponent extends CustomComponent implements CommandListener {

    private final String moduleName;

    public ModuleComponent(String moduleName) {

        this.moduleName = moduleName;
        CommandBuilder.setSource(moduleName);

        setHeight("100%");
        setCompositionRoot(getLayout());
    }

    public static Layout getLayout() {

        TextArea textArea = UIUtil.getTextArea(800, 600);
        UILogger logger = new UILogger(textArea);
        AbsoluteLayout layout = new AbsoluteLayout();

        layout.addComponent(UIUtil.getButton("Check Status", 120, new StatusChainBuilder(logger).getChain()), "left: 30px; top: 50px;");

        layout.addComponent(UIUtil.getLabel("<h3>Manage</h3>", 200, 40), "left: 30px; top: 90px;");
        layout.addComponent(UIUtil.getButton("Install", 120, new InstallChainBuilder(logger).getChain()), "left: 30px; top: 130px;");
        layout.addComponent(UIUtil.getButton("Remove", 120, new RemoveChainBuilder(logger).getChain()), "left: 30px; top: 170px;");

        layout.addComponent(UIUtil.getLabel("<h3>Service</h3>", 200, 40), "left: 30px; top: 220px;");
        layout.addComponent(UIUtil.getButton("Start", 120, ServiceChainBuilder.getStartChain(logger)), "left: 30px; top: 260px;");
        layout.addComponent(UIUtil.getButton("Stop", 120, ServiceChainBuilder.getStopChain(logger)), "left: 30px; top: 300px;");

        layout.addComponent(textArea, "left: 200px; top: 50px;");

        return layout;
    }

    @Override
    public void onCommand(Response response) {
        CommandExecutor.INSTANCE.onResponse(response);
    }

    @Override
    public String getName() {
        return moduleName;
    }

}