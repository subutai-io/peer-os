package org.safehaus.kiskis.mgmt.server.ui.modules.pig;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;

import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class Pig implements Module {

    private static final String MODULE_NAME = "Pig";
    private static TaskRunner taskRunner;

    public void setTaskRunner(TaskRunner taskRunner) {
        Pig.taskRunner = taskRunner;
    }

    public static TaskRunner getTaskRunner() {
        return taskRunner;
    }

    @Override
    public String getName() {
        return Pig.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(MODULE_NAME);
    }
}
