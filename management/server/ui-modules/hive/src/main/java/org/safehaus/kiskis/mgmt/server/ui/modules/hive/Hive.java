package org.safehaus.kiskis.mgmt.server.ui.modules.hive;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;

import org.safehaus.kiskis.mgmt.server.ui.modules.hive.view.ModuleComponent;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class Hive implements Module {

    private static final String MODULE_NAME = "Hive";
    private static TaskRunner taskRunner;

    public void setTaskRunner(TaskRunner taskRunner) {
        Hive.taskRunner = taskRunner;
    }

    public static TaskRunner getTaskRunner() {
        return taskRunner;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(MODULE_NAME);
    }
}
