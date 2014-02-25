package org.safehaus.kiskis.mgmt.server.ui.modules.lucene.view;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextArea;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.action.chain.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.command.CommandBuilder;

public class ModuleComponent extends CustomComponent {

    private final String moduleName;

    public ModuleComponent(String moduleName) {

        this.moduleName = moduleName;
        CommandBuilder.setSource(moduleName);
        CommandBuilder.setTimeout(60);

        setHeight("100%");
        setCompositionRoot(getLayout());
    }

    public static Layout getLayout() {

        TextArea textArea = UIUtil.getTextArea(800, 600);
        UILogger logger = new UILogger(textArea);
        AbsoluteLayout layout = new AbsoluteLayout();

        layout.addComponent(UIUtil.getButton("Check Status", 120, new StatusChainBuilder(logger).getChain()), "left: 30px; top: 50px;");
        layout.addComponent(UIUtil.getButton("Install", 120, new InstallChainBuilder(logger).getChain()), "left: 30px; top: 100px;");
        layout.addComponent(UIUtil.getButton("Remove", 120, new RemoveChainBuilder(logger).getChain()), "left: 30px; top: 150px;");

        layout.addComponent(textArea, "left: 200px; top: 50px;");

        return layout;
    }

}
